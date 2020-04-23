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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.syndesis.dv.model.DataVirtualization;
import io.syndesis.dv.server.DvService;
import io.syndesis.dv.server.V1Constants;

@RestController
@ConditionalOnProperty(value = "endpoints.test_support.enabled")
@RequestMapping( V1Constants.APP_PATH+V1Constants.FS+V1Constants.TEST_SUPPORT )
@Api( tags = {V1Constants.TEST_SUPPORT} )
public class TestSupportHandler extends DvService {

    @Autowired
    private DataVirtualizationService dataVirtualizationService;

    @GetMapping(value = "/reset-db")
    @ApiOperation(value = "Reset the persistent and running state.")
    @ApiResponses(value = {
        @ApiResponse(code = 400, message = "An error has occurred.")
    })
    public void resetDBToDefault() throws Exception {
        Iterable<? extends DataVirtualization> virtualizations = repositoryManager.runInTransaction(true, ()->{
            return repositoryManager.findDataVirtualizations();
        });

        for (DataVirtualization dv : virtualizations) {
            try {
                dataVirtualizationService.deletePublishedVirtualization(dv.getName());
                dataVirtualizationService.deleteDataVirtualization(dv.getName());
            } catch (Exception e) {
                LOGGER.info("Could not delete virtualization %s", e, dv.getName()); //$NON-NLS-1$
            }
        }
    }

}