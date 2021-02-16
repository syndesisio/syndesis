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
package io.syndesis.server.logging.jaeger.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.WebApplicationException;

import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.server.endpoint.v1.handler.activity.Activity;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

/*

To retrieve a new example-jaeger-trace-result.json

https://syndesis-jaeger-syndesis.192.168.42.160.nip.io/api/traces?limit=10&lookback=1h&service=i-LyeLhLb6-tEYiLoWuxEz

To retrieve a new expected-activities.json

https://syndesis-syndesis.192.168.42.160.nip.io/api/v1/activity/integrations/i-LyeLhLb6-tEYiLoWuxEz

 */
public class JaegerActivityTrackingServiceTest {

    @Test
    public void shouldRetainLastRetainActivityLogs() throws Exception {

        // Instead of testing an online service, lets override to use static test data.
        JaegerActivityTrackingService service = new JaegerActivityTrackingService(new JaegerQueryAPI("http://localhost:16686/api") {
            @Override
            public ArrayList<Trace> tracesForService(String service, int lookbackDays, int limit) {
                try {
                    String json = resource("example-jaeger-trace-result.json");
                    Traces traces = JsonUtils.reader().forType(Traces.class).readValue(json);
                    return traces.data;
                } catch (IOException e) {
                    throw new WebApplicationException(e);
                }
            }
        });

        List<Activity> activities = service.getActivities("test", null, null);
        assertThat(activities).isNotNull();

        String activitiesJson = JsonUtils.writer().withDefaultPrettyPrinter().writeValueAsString(activities).trim();
        // print the activitiesJson to replace the content of expected-activities.json
        // System.out.println(activitiesJson);
        String expectedActivitiesJson = resource("expected-activities.json").trim();
        JSONAssert.assertEquals(expectedActivitiesJson, activitiesJson, true);

    }


    private static String resource(String file) throws IOException {
        ClassLoader loader = JaegerActivityTrackingServiceTest.class.getClassLoader();
        try (InputStream is = requireNonNull(loader.getResourceAsStream(file))) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            copy(is, os);
            return new String(os.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private static void copy(InputStream is, ByteArrayOutputStream os) throws IOException {
        int c;
        while ((c = is.read()) >= 0) {
            os.write(c);
        }
    }


}
