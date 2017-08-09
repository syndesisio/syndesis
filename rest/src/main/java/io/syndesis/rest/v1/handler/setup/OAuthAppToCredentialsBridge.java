/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.rest.v1.handler.setup;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.persistence.EntityNotFoundException;

import io.syndesis.core.EventBus;
import io.syndesis.core.Json;
import io.syndesis.credential.Credentials;
import io.syndesis.model.ChangeEvent;
import io.syndesis.model.Kind;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * This component handles initializing and updating the CredentialProviderLocator
 * from the DB state.
 */
@Component
public class OAuthAppToCredentialsBridge {

    private static final Logger LOG = LoggerFactory.getLogger(OAuthAppToCredentialsBridge.class);

    private final OAuthAppHandler oAuthAppHandler;
    private final Credentials credentials;
    private final Optional<EventBus> bus;
    private ExecutorService executor;

    public OAuthAppToCredentialsBridge(OAuthAppHandler oAuthAppHandler, Credentials credentials, Optional<EventBus> bus) {
        this.oAuthAppHandler = oAuthAppHandler;
        this.credentials = credentials;
        this.bus = bus;
    }

    @PostConstruct
    public void init() {

        if( bus.isPresent() ) {
            executor = Executors.newSingleThreadExecutor();
            bus.get().subscribe(getClass().getName(), getChangeEventSubscription());
        }

        // lets look at the app configs stored in the DB...
        for (OAuthAppHandler.OAuthApp app : oAuthAppHandler.get()) {
            registerCredentialProvider(app);
        }
    }

    @PreDestroy
    public void shutdown() {
        if( bus.isPresent() ) {
            bus.get().unsubscribe(getClass().getName());
            executor.shutdown();
        }
    }

    private EventBus.Subscription getChangeEventSubscription() {
        return (event, data) -> {
            // Never do anything that could block in this callback!
            if (event!=null && "change-event".equals(event)) {
                try {
                    ChangeEvent changeEvent = Json.mapper().readValue(data, ChangeEvent.class);
                    if (changeEvent != null) {
                        changeEvent.getId().ifPresent(id -> {
                            changeEvent.getKind()
                                .map(Kind::from)
                                .filter(k -> k == Kind.Connector)
                                .ifPresent(k -> {
                                    executor.execute(() -> onConnectorUpdated(id));
                                });
                        });
                    }
                } catch (IOException e) {
                    LOG.error("Error while processing change-event {}", data, e);
                }
            }
        };
    }

    private void onConnectorUpdated(String connectorId) {
        OAuthAppHandler.OAuthApp oAuthApp = null;
        try {
            oAuthApp = oAuthAppHandler.get(connectorId);
        } catch (EntityNotFoundException e) {
            // Not all connectors will be oauth apps..
            return;
        }
        registerCredentialProvider(oAuthApp);
    }

    public void registerCredentialProvider(OAuthAppHandler.OAuthApp app) {
        // is the app configured?
        if( isSet(app.clientId) && isSet(app.clientSecret) ) {
            credentials.registerProvider(app.id, app);
        }
    }

    private static boolean isSet(String value) {
        return value!=null && !value.isEmpty();
    }
}
