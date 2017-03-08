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
package com.redhat.ipaas.rest.v1.handler.integration;

import java.io.IOException;

import javax.ws.rs.Path;

import com.redhat.ipaas.core.IPaasServerException;
import com.redhat.ipaas.dao.manager.DataManager;
import com.redhat.ipaas.github.GitHubService;
import com.redhat.ipaas.model.integration.Integration;
import com.redhat.ipaas.rest.v1.handler.BaseHandler;
import com.redhat.ipaas.rest.v1.operations.*;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("/integrations")
@Api(value = "integrations")
@Component
public class IntegrationHandler extends BaseHandler implements Lister<Integration>, Getter<Integration>, Creator<Integration>, Deleter<Integration>, Updater<Integration> {

    private GitHubService gitHubService;

    public IntegrationHandler(@Autowired DataManager dataMgr, @Autowired GitHubService gitHubService) {
        super(dataMgr);
        this.gitHubService = gitHubService;
    }

    @Override
    public Class<Integration> resourceClass() {
        return Integration.class;
    }

    @Override
    public String resourceKind() {
        return Integration.KIND;
    }

    @Override
    public Integration create(Integration integration) {
        ensureRepository(integration);
        return Creator.super.create(integration);
    }

    // ==========================================================================

    private void ensureRepository(Integration integration) {
        String repoName = gitHubService.sanitizeRepoName(integration.getName());
        try {
            gitHubService.ensureRepository(repoName);
        } catch (IOException e) {
            throw IPaasServerException.launderThrowable(e);
        }
    }
}
