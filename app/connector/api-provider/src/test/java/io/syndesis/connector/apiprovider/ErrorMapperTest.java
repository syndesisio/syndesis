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
package io.syndesis.connector.apiprovider;

import java.util.Collections;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import io.syndesis.common.util.ErrorCategory;
import io.syndesis.common.util.SyndesisConnectorException;

public class ErrorMapperTest {

    static final Map<String, String> errorResponseCodeMappings=ErrorMapper.jsonToMap("{\"SQL_CONNECTOR_ERROR\":\"500\",\"SERVER_ERROR\":\"500\",\"SQL_DATA_ACCESS_ERROR\":\"400\",\"SQL_ENTITY_NOT_FOUND_ERROR\":\"404\"}");

    @Test
    public void testJsonToMap() {
        Assert.assertEquals(4, errorResponseCodeMappings.size());
        Assert.assertTrue(errorResponseCodeMappings.containsKey("SQL_CONNECTOR_ERROR"));
    }

    @Test
    public void testEmptyMap() {
        SyndesisConnectorException sce = new SyndesisConnectorException("SQL_CONNECTOR_ERROR", "error msg test");
        ErrorStatusInfo info = ErrorMapper.mapError(sce, Collections.emptyMap(), 200);
        Assert.assertEquals(Integer.valueOf(200)  ,info.getResponseCode());
        Assert.assertEquals("SQL_CONNECTOR_ERROR" ,info.getCategory());
        Assert.assertEquals("error msg test"      ,info.getMessage());
    }

    @Test
    public void testSyndesisEntityNotFOund() {
        Exception e = new SyndesisConnectorException("SQL_ENTITY_NOT_FOUND_ERROR", "entity not found");
        ErrorStatusInfo info = ErrorMapper.mapError(e, errorResponseCodeMappings, 200);
        Assert.assertEquals(Integer.valueOf(404),        info.getResponseCode());
        Assert.assertEquals("SQL_ENTITY_NOT_FOUND_ERROR",info.getCategory());
        Assert.assertEquals("entity not found"          ,info.getMessage());
    }

    @Test
    public void testUnmappedException() {
        Exception e = new Exception("Unmapped Exception");
        ErrorStatusInfo info = ErrorMapper.mapError(e, errorResponseCodeMappings, 200);
        Assert.assertEquals(Integer.valueOf(500)         ,info.getResponseCode());
        Assert.assertEquals(ErrorCategory.SERVER_ERROR   ,info.getCategory());
        Assert.assertEquals("Unmapped Exception"         ,info.getMessage());
    }
}
