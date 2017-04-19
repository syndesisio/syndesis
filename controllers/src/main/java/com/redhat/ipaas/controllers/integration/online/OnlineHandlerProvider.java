/*
 *
 *  Copyright (C) 2016 Red Hat, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.redhat.ipaas.controllers.integration.online;

import java.util.Arrays;
import java.util.List;

import com.redhat.ipaas.controllers.integration.StatusChangeHandlerProvider;
import com.redhat.ipaas.dao.manager.DataManager;
import com.redhat.ipaas.github.GitHubService;
import com.redhat.ipaas.openshift.OpenShiftService;
import com.redhat.ipaas.project.converter.ProjectGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "controllers.integration.enabled", havingValue = "true", matchIfMissing = true)
public class OnlineHandlerProvider implements StatusChangeHandlerProvider {

    private final DataManager dataManager;
    private final OpenShiftService openShiftService;
    private final GitHubService gitHubService;
    private final ProjectGenerator projectConverter;

    public OnlineHandlerProvider(DataManager dataManager, OpenShiftService openShiftService,
                                 GitHubService gitHubService, ProjectGenerator projectConverter) {
        this.dataManager = dataManager;
        this.openShiftService = openShiftService;
        this.gitHubService = gitHubService;
        this.projectConverter = projectConverter;
    }

    @Override
    public List<StatusChangeHandler> getStatusChangeHandlers() {
        return Arrays.asList(
            new ActivateHandler(dataManager, openShiftService, gitHubService, projectConverter),
            new DeactivateHandler(openShiftService),
            new DeleteHandler(openShiftService));
    }
}
