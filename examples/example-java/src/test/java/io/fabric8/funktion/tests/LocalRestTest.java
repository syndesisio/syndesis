/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fabric8.funktion.tests;

import io.fabric8.funktion.runtime.Main;
import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.HttpURLConnection;

import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.containsString;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LocalRestTest {
    private static final transient Logger LOG = LoggerFactory.getLogger(LocalRestTest.class);

    @LocalServerPort
    protected int serverPort;

    @Before
    public void init() {
        LOG.info("Testing on serverPort: " + serverPort);

        // port from the funktion.yml
        RestAssured.port = serverPort;
    }

    @Test
    public void testFunktionEndpoint() throws Exception {
        String name = "James";
        when()
                .get("/?name=" + name)
                .then()
                .statusCode(HttpURLConnection.HTTP_OK)
                .body(containsString("Hello " + name));
    }

}