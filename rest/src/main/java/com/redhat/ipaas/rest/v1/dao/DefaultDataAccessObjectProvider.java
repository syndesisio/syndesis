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
package com.redhat.ipaas.rest.v1.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DefaultDataAccessObjectProvider implements DataAccessObjectProvider {

    private final IntegrationDAO integrationDAO;

    private final IntegrationPatternDAO integrationPatternDAO;

    @Autowired
    public DefaultDataAccessObjectProvider(IntegrationDAO integrationDAO, IntegrationPatternDAO integrationPatternDAO) {
        this.integrationDAO = integrationDAO;
        this.integrationPatternDAO = integrationPatternDAO;
    }

    public List<DataAccessObject> getDataAccessObjects() {
        return Arrays.asList(integrationDAO, integrationPatternDAO);
    }
}
