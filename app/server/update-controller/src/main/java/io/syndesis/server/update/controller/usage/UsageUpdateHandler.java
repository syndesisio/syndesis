/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.server.update.controller.usage;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.syndesis.common.model.ChangeEvent;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.WithId;
import io.syndesis.common.model.WithUsage;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.extension.Extension;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.update.controller.ResourceUpdateHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Functions;

public final class UsageUpdateHandler implements ResourceUpdateHandler {

    private static final Logger LOG = LoggerFactory.getLogger(UsageUpdateHandler.class);

    private static final EnumSet<Kind> SUPPORTED = EnumSet.of(Kind.Integration, Kind.Extension);

    private final DataManager dataManager;

    public UsageUpdateHandler(final DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public boolean canHandle(final ChangeEvent event) {
        final Optional<String> maybeKind = event.getKind();
        if (!maybeKind.isPresent()) {
            return false;
        }

        final Kind kind = Kind.from(maybeKind.get());
        return SUPPORTED.contains(kind);
    }

    @Override
    public void process(final ChangeEvent event) {
        LOG.debug("Received event: {}", event);
        processInternal(event);
    }

    void processInternal(final ChangeEvent event) {
        LOG.debug("Processing event: {}", event);

        final ListResult<Integration> integrationsResult = dataManager.fetchAll(Integration.class);

        final List<Integration> integrations = integrationsResult.getItems();

        updateUsageFor(Connection.class, integrations, Integration::getConnectionIds, Functions.compose(Optional::get, Connection::getId),
            UsageUpdateHandler::withUpdatedUsage);

        updateUsageFor(Extension.class, integrations, Integration::getExtensionIds, Extension::getExtensionId,
            UsageUpdateHandler::withUpdatedUsage);
    }

    private <T extends WithId<T> & WithUsage> void updateUsageFor(final Class<T> type, final List<Integration> integrations,
        final Function<Integration, Set<String>> idsFunction, final Function<T, String> idFunction, final BiFunction<T, Integer, T> usageUpdater) {
        final Map<String, Long> usage = computeUsage(integrations, idsFunction);

        final ListResult<T> result = dataManager.fetchAll(type);

        final List<T> allItems = result.getItems();

        for (final T item : allItems) {
            final String id = idFunction.apply(item);

            final int recordedUse = item.getUses();
            final int currentUse = usage.getOrDefault(id, 0L).intValue();

            if (recordedUse != currentUse) {
                LOG.debug("Updating usage from: {} to: {}", recordedUse, currentUse);
                dataManager.update(usageUpdater.apply(item, currentUse));
            }
        }
    }

    static Connection withUpdatedUsage(final Connection connection, final int currentUse) {
        return new Connection.Builder().createFrom(connection).uses(currentUse).build();
    }

    static Extension withUpdatedUsage(final Extension extension, final int currentUse) {
        return new Extension.Builder().createFrom(extension).uses(currentUse).build();
    }

    private static Map<String, Long> computeUsage(final List<Integration> integrations, final Function<Integration, Set<String>> idFunction) {
        return integrations.stream()
            .filter(i -> !i.isDeleted())
            .flatMap(i -> idFunction.apply(i).stream())
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

}
