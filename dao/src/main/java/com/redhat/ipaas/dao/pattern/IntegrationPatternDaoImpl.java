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
package com.redhat.ipaas.dao.pattern;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import com.redhat.ipaas.dao.IntegrationPatternDao;
import com.redhat.ipaas.model.ListResult;
import com.redhat.ipaas.model.WithId;
import com.redhat.ipaas.model.integration.IntegrationPattern;

@Named(IntegrationPattern.KIND)
@ApplicationScoped
public class IntegrationPatternDaoImpl implements IntegrationPatternDao {

    private static final Map<String, IntegrationPattern> PATTERNS = new HashMap<>();

    private static void register(IntegrationPattern pattern) {
        PATTERNS.put(pattern.getId().orElseThrow(()->new IllegalStateException("Integration Pattern requires an ID.")), pattern);
    }

    public IntegrationPatternDaoImpl() {
        register(IntegrationPattern.CHOICE);
        register(IntegrationPattern.OTHERWISE);
        register(IntegrationPattern.SPLITTER);
        register(IntegrationPattern.THROTTLER);
        register(IntegrationPattern.MESSAGE_FILTER);
        register(IntegrationPattern.SET_BODY);
        register(IntegrationPattern.SET_HEDER);
    }

    @Override
    public Class<IntegrationPattern> getType() {
        return IntegrationPattern.class;
    }

    @Override
    public IntegrationPattern fetch(String id) {
        return PATTERNS.get(id);
    }

    @Override
    public ListResult<IntegrationPattern> fetchAll() {
        return new ListResult.Builder<IntegrationPattern>()
            .items(PATTERNS.values())
            .totalCount(PATTERNS.size())
            .build();
    }

    @Override
    public IntegrationPattern create(IntegrationPattern entity) {
        throw new UnsupportedOperationException("You can't create an Integration Pattern.");
    }

    @Override
    public IntegrationPattern update(IntegrationPattern entity) {
        throw new UnsupportedOperationException("You can't modify an Integration Pattern.");
    }

    @Override
    public boolean delete(WithId<IntegrationPattern> entity) {
        throw new UnsupportedOperationException("You can't delete an Integration Pattern.");
    }

    @Override
    public boolean delete(String id) {
        throw new UnsupportedOperationException("You can't delete an Integration Pattern.");
    }
}
