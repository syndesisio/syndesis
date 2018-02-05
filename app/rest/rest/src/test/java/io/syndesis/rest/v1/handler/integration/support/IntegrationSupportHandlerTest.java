package io.syndesis.rest.v1.handler.integration.support;

import io.syndesis.core.Json;
import io.syndesis.model.ModelExport;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.mockito.Mockito.*;

public class IntegrationSupportHandlerTest {


    @Test
    public void verifyJacksonBehaviorWithSourceStreams() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        // default config closes source stream
        try(InputStream fis = spy(new FileInputStream(new File(classLoader.getResource("model.json").getFile())))){
            ModelExport models = Json.mapper().readValue(fis, ModelExport.class);
            verify(fis, times(1)).close();
        }

        // disabling feature inline, skipt closing source stream
        try(InputStream fis = spy(new FileInputStream(new File(classLoader.getResource("model.json").getFile())))){
            ModelExport models = Json.mapperWithoutSourceAutoclose().readValue(fis, ModelExport.class);
            verify(fis, times(0)).close();
        }
    }
}
