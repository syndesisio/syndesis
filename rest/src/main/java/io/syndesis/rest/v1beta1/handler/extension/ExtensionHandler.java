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
package io.syndesis.rest.v1beta1.handler.extension;

import io.swagger.annotations.Api;
import io.syndesis.core.KeyGenerator;
import io.syndesis.core.SyndesisServerException;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.filestore.FileStore;
import io.syndesis.model.Kind;
import io.syndesis.model.extension.Extension;
import io.syndesis.rest.v1.handler.BaseHandler;
import io.syndesis.rest.v1.operations.Deleter;
import io.syndesis.rest.v1.operations.Getter;
import io.syndesis.rest.v1.operations.Lister;
import io.syndesis.rest.v1beta1.util.ExtensionAnalyzer;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.io.InputStream;

@Path("/extensions")
@Api(value = "extensions")
@Component
public class ExtensionHandler extends BaseHandler implements Lister<Extension>, Getter<Extension>, Deleter<Extension> {

    private final FileStore fileStore;

    private final ExtensionAnalyzer extensionAnalyzer;

    public ExtensionHandler(final DataManager dataMgr, final FileStore fileStore,
                            final ExtensionAnalyzer extensionAnalyzer) {
        super(dataMgr);
        this.fileStore = fileStore;
        this.extensionAnalyzer = extensionAnalyzer;
    }

    @Override
    public Kind resourceKind() {
        return Kind.Extension;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public Extension upload(@Context SecurityContext sec, MultipartFormDataInput dataInput) {

        String id = KeyGenerator.createKey();
        String fileLocation = "/extensions/" + id;

        try {
            storeFile(fileLocation, dataInput);

            Extension embeddedExtension = extractExtension(fileLocation);

            Extension extension = new Extension.Builder()
                .createFrom(embeddedExtension)
                .id(id)
                .build();

            // TODO: VALIDATE

            return getDataManager().create(extension);
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception ex) {
            try {
                delete(id);
            } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception dex) {
                // ignore
            }
            throw SyndesisServerException.launderThrowable(ex);
        }
    }

    @Override
    public void delete(String id) {
        Deleter.super.delete(id);
        fileStore.delete("/extensions/" + id);
    }

    // ===============================================================

    @Nonnull
    private Extension extractExtension(String location) {
        try (InputStream file = fileStore.read(location)) {
            return extensionAnalyzer.analyze(file);
        } catch (IOException ex) {
            throw SyndesisServerException.
                launderThrowable("Unable to load extension from filestore location " + location, ex);
        }
    }

    private void storeFile(String location, MultipartFormDataInput dataInput) {
        // Store the artifact into the filestore
        try (InputStream file = getBinaryArtifact(dataInput)) {
            fileStore.write(location, file);
        } catch (IOException ex) {
            throw SyndesisServerException.launderThrowable("Unable to store the file into the filestore", ex);
        }
    }

    @Nonnull
    private InputStream getBinaryArtifact(MultipartFormDataInput input) {
        if (input == null || input.getParts() == null || input.getParts().isEmpty()) {
            throw new IllegalArgumentException("Multipart request is empty");
        }

        try {
            InputStream result;
            if (input.getParts().size() == 1) {
                InputPart filePart = input.getParts().iterator().next();
                result = filePart.getBody(InputStream.class, null);
            } else {
                result = input.getFormDataPart("file", InputStream.class, null);
            }

            if (result == null) {
                throw new IllegalArgumentException("Can't find a valid 'file' part in the multipart request");
            }

            return result;
        } catch (IOException e) {
            throw new IllegalArgumentException("Error while reading multipart request", e);
        }
    }

}
