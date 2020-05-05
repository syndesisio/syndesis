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

package io.syndesis.dv.server.endpoint;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.syndesis.dv.StringConstants;
import io.syndesis.dv.server.DvConfigurationProperties;
import io.syndesis.dv.server.DvService;
import io.syndesis.dv.server.SSOConfigurationProperties;
import io.syndesis.dv.server.V1Constants;

@RestController
@RequestMapping(value = V1Constants.APP_PATH
        + StringConstants.FS + V1Constants.STATUS)
@Api(tags = { V1Constants.STATUS })
public class StatusService extends DvService {

    @Autowired
    private SSOConfigurationProperties ssoConfigurationProperties;
    @Autowired
    private DvConfigurationProperties dvConfigurationProperties;

    @GetMapping(path = V1Constants.ROLES, produces= { MediaType.APPLICATION_JSON_VALUE })
    @ApiOperation(value = "Return all role names in use",
        response = String.class, responseContainer = "List")
    @ApiResponses(value = { @ApiResponse(code = 403, message = "An error has occurred."),
    @ApiResponse(code = 503, message = "Security Not Configured")})
    public List<String> getRoles() throws Exception {
        return repositoryManager.runInTransaction(false, () -> {
            ArrayList<String> result = new ArrayList<>(repositoryManager
                    .findRoleNames());
            //without explicit role management, any authenticated becomes
            //a special / reserved role name
            //this is a little hack-ish as this approach is not easily localized
            //and creates a reserved name
            result.add(ServiceVdbGenerator.ANY_AUTHENTICATED);
            return result;
        });
    }

    @GetMapping(produces= { MediaType.APPLICATION_JSON_VALUE })
    @ApiOperation(value = "Get the server status",
        response = DvStatus.class)
    public DvStatus getStatus() {
        DvStatus result = new DvStatus();
        result.setExposeVia3scale(dvConfigurationProperties.isExposeVia3scale());
        result.setSsoConfigured(ssoConfigurationProperties.getAuthServerUrl() != null);
        return result;
    }

}
