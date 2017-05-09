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
package io.syndesis.openshift;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DockerImageTest {

    @Test
    public void testDockerImageParsing() throws Exception {
        String data[] = {
            "bla", "bla", "bla", "latest",

            "bla/foo", "bla/foo", "foo", "latest",

            "bla/foo:2.0.0", "bla/foo:2.0.0", "foo", "2.0.0",
        };

        for (int i = 0; i < data.length; i +=4) {
            OpenShiftServiceImpl.DockerImage img = new OpenShiftServiceImpl.DockerImage(data[i]);
            assertEquals(img.getImage(), data[i+1]);
            assertEquals(img.getShortName(), data[i+2]);
            assertEquals(img.getTag(), data[i+3]);
        }
    }

}
