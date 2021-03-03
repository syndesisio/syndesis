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
package io.syndesis.connector.support.processor;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.FileAlreadyExistsException;
import java.util.Collections;
import java.util.Map;

import org.apache.camel.component.file.GenericFileOperationFailedException;
import org.junit.Test;

import io.syndesis.common.util.CamelCase;
import io.syndesis.common.util.ErrorCategory;
import io.syndesis.common.util.SyndesisConnectorException;

public class ErrorMapperTest {

    static final Map<String, Integer> errorResponseCodeMappings=
            ErrorMapper.jsonToMap("{\"CONNECTOR_ERROR\":500,\"SERVER_ERROR\":500,"
                    + "\"DATA_ACCESS_ERROR\":400,\"DUPLICATE_KEY_ERROR\":409,\"ENTITY_NOT_FOUND_ERROR\":404}");

    @Test
    public void testJsonToMap() {
        assertThat(errorResponseCodeMappings.size()).isEqualTo(5);
        assertThat(errorResponseCodeMappings.containsKey("CONNECTOR_ERROR")).isTrue();
    }

    @Test
    public void testEmptyMap() {
        SyndesisConnectorException sce = new SyndesisConnectorException("CONNECTOR_ERROR", "error msg test");
        ErrorStatusInfo info = ErrorMapper.mapError(sce, Collections.emptyMap(), 200);
        assertThat(info.getHttpResponseCode()).isEqualTo(200);
        assertThat(info.getCategory()).isEqualTo("CONNECTOR_ERROR");
        assertThat(info.getMessage()).isEqualTo("error msg test");
    }

    @Test
    public void testSyndesisEntityNotFOund() {
        Exception e = new SyndesisConnectorException("ENTITY_NOT_FOUND_ERROR", "entity not found");
        ErrorStatusInfo info = ErrorMapper.mapError(e, errorResponseCodeMappings, 200);
        assertThat(info.getHttpResponseCode()).isEqualTo(404);
        assertThat(info.getCategory()).isEqualTo("ENTITY_NOT_FOUND_ERROR");
        assertThat(info.getMessage()).isEqualTo("entity not found");
    }

    @Test
    public void testUnmappedException() {
        Exception e = new Exception("Unmapped Exception");
        ErrorStatusInfo info = ErrorMapper.mapError(e, errorResponseCodeMappings, 200);
        assertThat(info.getHttpResponseCode()).isEqualTo(500);
        assertThat(info.getCategory()).isEqualTo(ErrorCategory.SERVER_ERROR);
        assertThat(info.getMessage()).isEqualTo("Unmapped Exception");
    }

    @Test
    public void testFileAlreadyExistsException() {
        Exception e = new GenericFileOperationFailedException("myfile");
        ErrorStatusInfo info = ErrorMapper.mapError(e, errorResponseCodeMappings, 200);
        assertThat(info.getHttpResponseCode()).isEqualTo(409);
        assertThat(info.getCategory()).isEqualTo("DUPLICATE_KEY_ERROR");
        assertThat(info.getMessage()).isEqualTo("myfile");
        assertThat(info.getError()).isEqualTo("generic_file_operation_failed_error");
    }

    @Test
    public void testCamelCaseToUnderscore() {
        Exception e = new FileAlreadyExistsException("myfile");
        String upperUnderscore = CamelCase.toUnderscore(e.getClass().getSimpleName());
        assertThat(upperUnderscore).isEqualTo("File_Already_Exists_Exception");
    }
}
