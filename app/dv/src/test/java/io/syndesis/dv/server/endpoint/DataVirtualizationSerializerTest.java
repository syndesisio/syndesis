/*
 * Copyright (C) 2013 Red Hat, Inc.
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

import static org.junit.Assert.*;

import java.net.URLDecoder;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import io.syndesis.dv.model.DataVirtualization;
import io.syndesis.dv.rest.JsonMarshaller;

@SuppressWarnings( { "javadoc", "nls" } )
public final class DataVirtualizationSerializerTest {

    protected static final String DATASERVICE_NAME = "dataservice1";

    private static final String DESCRIPTION = "my description";

    private static final String JSON = "{\n" +
            "  \"name\" : \"dataservice1\",\n" +
            "  \"description\" : \"my description\",\n" +
            "  \"publishedState\" : \"NOTFOUND\",\n" +
            "  \"empty\" : true,\n" +
            "  \"modified\" : false\n" +
            "}";

    private RestDataVirtualization dataVirtualization;

    @Before
    public void init() throws Exception {
        DataVirtualization theService = Mockito.mock(DataVirtualization.class);
        Mockito.when(theService.getName()).thenReturn(DATASERVICE_NAME);

        this.dataVirtualization = new RestDataVirtualization(theService);
        this.dataVirtualization.setDescription(DESCRIPTION);
    }

    @Test
    public void shouldExportJson() throws Exception {
        String json = JsonMarshaller.marshall( this.dataVirtualization );
        json = URLDecoder.decode(json, "UTF-8");
        assertEquals(JSON, json);
    }

    @Test
    public void shouldImportJson() {
        final RestDataVirtualization descriptor = JsonMarshaller.unmarshall( JSON, RestDataVirtualization.class );
        assertEquals(DATASERVICE_NAME, descriptor.getName());
        assertEquals(DESCRIPTION, descriptor.getDescription());
    }

    @Test( expected = Exception.class )
    public void shouldNotImportJsonWhenIdIsMissing() {
        final String malformed = "{\"description\":\"my description\",\"links\":[{\"rel\":\"self\",\"href\":\"http://localhost:8080/v1/workspace/vdbs/MyVdb\",\"method\":\"GET\"},{\"rel\":\"parent\",\"href\":\"http://localhost:8080/v1/workspace/vdbs\",\"method\":\"GET\"},{\"rel\":\"manifest\",\"href\":\"http://localhost:8080/v1/workspace/vdbs/MyVdb/manifest\",\"method\":\"GET\"}]}";
        JsonMarshaller.unmarshall( malformed, RestDataVirtualization.class );
    }

}
