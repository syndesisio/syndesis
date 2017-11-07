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
package io.syndesis.controllers.extension;

import io.syndesis.dao.manager.DataManager;
import io.syndesis.filestore.FileStore;
import io.syndesis.model.extension.Extension;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExtensionDataProviderTest {

    @Test
    public void shouldGetCorrectDataByExtensionId() throws IOException {
        final DataManager dataManager = mock(DataManager.class);
        final FileStore fileStore = mock(FileStore.class);
        ExtensionDataProvider extensionDataProvider = new ExtensionDataProvider(dataManager, fileStore);

        String sampleBinaryData = "Hello";
        Extension sampleMetadata = new Extension.Builder()
            .id("1234")
            .status(Extension.Status.Installed)
            .extensionId("extensionId")
            .description("Description")
            .build();

        when(fileStore.read("/extensions/1234"))
            .thenReturn(new ByteArrayInputStream(sampleBinaryData.getBytes(StandardCharsets.UTF_8)));
        when(dataManager.fetchIdsByPropertyValue(Extension.class, "extensionId", "extensionId",
            "status", Extension.Status.Installed.name())).thenReturn(Collections.singleton("1234"));
        when(dataManager.fetch(Extension.class, "1234")).thenReturn(sampleMetadata);

        assertThat(extensionDataProvider.getExtensionMetadata("extensionId")).isEqualTo(sampleMetadata);
        assertThat(IOUtils.toString(
            extensionDataProvider.getExtensionBinaryFile("extensionId"), StandardCharsets.UTF_8)
        ).isEqualTo(sampleBinaryData);
    }
}
