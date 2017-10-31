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
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.syndesis.core.KeyGenerator;
import io.syndesis.core.SyndesisServerException;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.filestore.FileStore;
import io.syndesis.model.Kind;
import io.syndesis.model.extension.Extension;
import io.syndesis.model.validation.AllValidations;
import io.syndesis.model.validation.NonBlockingValidations;
import io.syndesis.rest.v1.handler.BaseHandler;
import io.syndesis.rest.v1.operations.Deleter;
import io.syndesis.rest.v1.operations.Getter;
import io.syndesis.rest.v1.operations.Lister;
import io.syndesis.rest.v1.operations.Violation;
import io.syndesis.rest.v1beta1.util.ExtensionAnalyzer;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/extensions")
@Api(value = "extensions")
@Component
public class ExtensionHandler extends BaseHandler implements Lister<Extension>, Getter<Extension>, Deleter<Extension> {

    private final FileStore fileStore;

    private final ExtensionAnalyzer extensionAnalyzer;

    private final Validator validator;

    public ExtensionHandler(final DataManager dataMgr, final FileStore fileStore,
                            final ExtensionAnalyzer extensionAnalyzer, final Validator validator) {
        super(dataMgr);
        this.fileStore = fileStore;
        this.extensionAnalyzer = extensionAnalyzer;
        this.validator = validator;
    }

    @Override
    public Kind resourceKind() {
        return Kind.Extension;
    }

    public Validator getValidator() {
        return validator;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public Extension upload(MultipartFormDataInput dataInput) {

        String id = KeyGenerator.createKey();
        String fileLocation = "/extensions/" + id;

        try {
            storeFile(fileLocation, dataInput);

            Extension embeddedExtension = extractExtension(fileLocation);

            Extension extension = new Extension.Builder()
                .createFrom(embeddedExtension)
                .id(id)
                .build();

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
        // Not a real delete of the extension: changing the status to Deleted
        Extension extension = getDataManager().fetch(Extension.class, id);
        this.doDelete(extension);
    }

    @POST
    @Path("/{id}/validation")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({
        @ApiResponse(code = 200, message = "All blocking validations pass", responseContainer = "Set",
            response = Violation.class),
        @ApiResponse(code = 400, message = "Found violations in validation", responseContainer = "Set",
            response = Violation.class)
    })
    public Set<Violation> validate(@NotNull @PathParam("id") final String extensionId) {
        Extension extension = getDataManager().fetch(Extension.class, extensionId);
        return doValidate(extension);
    }

    @POST
    @Path(value = "/validation")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses({
        @ApiResponse(code = 200, message = "All blocking validations pass", responseContainer = "Set",
            response = Violation.class),
        @ApiResponse(code = 400, message = "Found violations in validation", responseContainer = "Set",
            response = Violation.class)
    })
    public Set<Violation> validate(@NotNull final Extension extension) {
        return doValidate(extension);
    }

    @POST
    @Path(value = "/{id}/install")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Installed"),
        @ApiResponse(code = 400, message = "Found violations in validation", responseContainer = "Set",
            response = Violation.class)
    })
    public void install(@NotNull @PathParam("id") final String id) {
        Extension extension = getDataManager().fetch(Extension.class, id);
        doValidate(extension);

        // Uninstall other active extensions
        doDeleteInstalled(extension.getExtensionId());

        getDataManager().update(new Extension.Builder().createFrom(extension)
            .status(Extension.Status.Installed)
            .build());
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

    private Set<Violation> doValidate(Extension extension) {
        final Set<ConstraintViolation<Extension>> constraintViolations = getValidator().validate(extension, Default.class, AllValidations.class);

        if (!constraintViolations.isEmpty()) {
            throw new ConstraintViolationException(constraintViolations);
        }

        Set<ConstraintViolation<Extension>> warnings = getValidator().validate(extension, NonBlockingValidations.class);
        return warnings.stream()
            .map(Violation.Builder::fromConstraintViolation)
            .collect(Collectors.toSet());
    }

    private void doDeleteInstalled(String logicalExtensionId) {
        Set<String> ids = getDataManager().fetchIdsByPropertyValue(Extension.class, "extensionId", logicalExtensionId);
        for (String id : ids) {
            Extension extension = getDataManager().fetch(Extension.class, id);
            if (extension.getStatus().isPresent() && extension.getStatus().get() == Extension.Status.Installed) {
                doDelete(extension);
            }
        }
    }

    private void doDelete(Extension extension) {
        getDataManager().update(new Extension.Builder()
            .createFrom(extension)
            .status(Extension.Status.Deleted)
            .build());
    }

}
