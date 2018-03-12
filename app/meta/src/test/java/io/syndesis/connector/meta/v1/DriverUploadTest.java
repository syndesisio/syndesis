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
package io.syndesis.connector.meta.v1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.junit.Ignore;
import org.junit.Test;

public class DriverUploadTest {

    final private static String FILE_NAME = "syndesis-library-jdbc-1.0.0.jar";
    final private static String FILE_CLASSPATH = "drivers/" + FILE_NAME;

    @Test
    public void upload() throws IOException, URISyntaxException {
        URL driverClasspath = DriverUploadTest.class.getClassLoader().getResource(FILE_CLASSPATH);
        assertThat(driverClasspath).isNotNull();

        MultipartFormDataInput multiPart = mock(MultipartFormDataInput.class);
        InputPart inputPartFileContent = mock(InputPart.class);
        InputPart inputPartFileName = mock(InputPart.class);

        List<InputPart> parts = new ArrayList<>();
        parts.add(inputPartFileContent);
        parts.add(inputPartFileName);
        when(multiPart.getParts()).thenReturn(parts);
        when(multiPart.getFormDataPart("file", InputStream.class, null)).thenReturn(driverClasspath.openStream());
        when(multiPart.getFormDataPart("fileName", String.class, null)).thenReturn(FILE_NAME);

        String tempDir = System.getProperty("java.io.tmpdir");
        System.setProperty("LOADER_HOME", tempDir);
        DriverUploadEndpoint endpoint = new DriverUploadEndpoint();
        Boolean reply = endpoint.upload(multiPart);
        assertThat(reply).isTrue();

        //check equality with the original file
        //File originalFile = new File(driverClasspath.toURI());
        //File uploadedFile = new File(tempDir + File.separator + FILE_NAME);
        //Boolean isEquals = FileUtils.contentEquals(originalFile, uploadedFile);
        //assertThat(isEquals).isTrue();
    }

    @Test @Ignore //Easy way to quickly upload an extension when running Application.main()
    public void upload2Server() throws IOException {

        URL driverClasspath = DriverUploadTest.class.getClassLoader().getResource(FILE_CLASSPATH);
        assertThat(driverClasspath).isNotNull();

        final WebTarget target = ClientBuilder.newClient()
                .target(String.format("http://%s/api/v1/drivers",
                        "localhost:8080"));

        MultipartFormDataOutput multipart = new MultipartFormDataOutput();
        multipart.addFormData("fileName", "myExtDir", MediaType.TEXT_PLAIN_TYPE);
        multipart.addFormData("file", driverClasspath.openStream(), MediaType.APPLICATION_OCTET_STREAM_TYPE);

        GenericEntity<MultipartFormDataOutput> entity = new GenericEntity<MultipartFormDataOutput>(multipart) {};
        target.request().post(Entity.entity(entity, MediaType.MULTIPART_FORM_DATA_TYPE),Boolean.class);

    }
}
