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

package io.syndesis.dv.openshift;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.syndesis.dv.openshift.BuildStatus.RouteStatus;
import io.syndesis.dv.openshift.BuildStatus.Status;
import io.syndesis.dv.rest.JsonMarshaller;

@SuppressWarnings("nls")
public class StatusTest {

    @Test public void testJsonRoundtrip() throws Exception {
        BuildStatus bs = new BuildStatus("vdb");
        bs.setName("buildName");
        bs.setStatus(Status.COMPLETE);
        bs.setVersion(2l);
        bs.setNamespace("namespace");
        bs.setPublishPodName("pod");
        //not used by serialization
        bs.setPublishConfiguration(new PublishConfiguration());
        RouteStatus route = new RouteStatus("x", ProtocolType.JDBC);
        route.setHost("host");
        route.setPath("path");
        route.setPort("port");
        route.setSecure(true);
        route.setTarget("target");

        String value = JsonMarshaller.marshall(bs);
        assertEquals("{\n" +
                "  \"status\" : \"COMPLETE\",\n" +
                "  \"name\" : \"buildName\",\n" +
                "  \"namespace\" : \"namespace\",\n" +
                "  \"lastUpdated\" : 0,\n" +
                "  \"openShiftName\" : \"vdb\",\n" +
                "  \"version\" : 2\n" +
                "}", value);
    }

}
