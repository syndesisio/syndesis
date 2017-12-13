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
package io.syndesis.rest.v1.handler.extension;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.syndesis.core.KeyGenerator;
import io.syndesis.core.SyndesisServerException;
import io.syndesis.dao.extension.ExtensionDataAccessObject;
import io.syndesis.dao.manager.DataManager;
import io.syndesis.model.Kind;
import io.syndesis.model.ListResult;
import io.syndesis.model.ResourceIdentifier;
import io.syndesis.model.extension.Extension;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.integration.Step;
import io.syndesis.model.validation.AllValidations;
import io.syndesis.model.validation.NonBlockingValidations;
import io.syndesis.rest.v1.SyndesisRestException;
import io.syndesis.rest.v1.handler.BaseHandler;
import io.syndesis.rest.v1.operations.Deleter;
import io.syndesis.rest.v1.operations.Getter;
import io.syndesis.rest.v1.operations.Lister;
import io.syndesis.model.Violation;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/extensions")
@Api(value = "extensions")
@Component
@ConditionalOnBean(ExtensionDataAccessObject.class)
public class ExtensionHandler extends BaseHandler implements Lister<Extension>, Getter<Extension>, Deleter<Extension> {

    private final ExtensionDataAccessObject fileStore;

    private final ExtensionAnalyzer extensionAnalyzer;

    private final Validator validator;

    public ExtensionHandler(final DataManager dataMgr,
                            final ExtensionDataAccessObject fileStore,
                            final ExtensionAnalyzer extensionAnalyzer,
                            final Validator validator) {
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
    public Extension upload(MultipartFormDataInput dataInput, @Context SecurityContext sec, @QueryParam("updatedId") String updatedId) {
        Date rightNow = new Date();
        String id = KeyGenerator.createKey();
        String fileLocation = "/extensions/" + id;

        try {
            storeFile(fileLocation, dataInput);

            Extension embeddedExtension = extractExtension(fileLocation);

            if (updatedId != null) {
                // Update
                Extension replacedExtension = getDataManager().fetch(Extension.class, updatedId);
                if (!replacedExtension.getExtensionId().equals(embeddedExtension.getExtensionId())) {
                    String message = "The uploaded extensionId (" + embeddedExtension.getExtensionId() +
                        ") does not match the existing extensionId (" + replacedExtension.getExtensionId() + ")";
                    throw new SyndesisRestException(message, message, null, Response.Status.BAD_REQUEST.getStatusCode());
                }
            } else {
                // New import
                Set<String> ids = getDataManager().fetchIdsByPropertyValue(Extension.class,
                    "extensionId", embeddedExtension.getExtensionId(),
                    "status", Extension.Status.Installed.name());

                if (!ids.isEmpty()) {
                    String message = "An extension with the same extensionId (" + embeddedExtension.getExtensionId() +
                        ") is already installed. Please update the existing extension instead of importing a new one";
                    throw new SyndesisRestException(message, message, null, Response.Status.BAD_REQUEST.getStatusCode());
                }

            }

            Extension extension = new Extension.Builder()
                .createFrom(embeddedExtension)
                .id(id)
                .status(Extension.Status.Draft)
                .uses(OptionalInt.empty())
                .lastUpdated(rightNow)
                .createdDate(rightNow)
                .userId(sec.getUserPrincipal().getName())
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
        Date rightNow = new Date();
        Extension extension = getDataManager().fetch(Extension.class, id);
        doValidate(extension);

        // Uninstall other active extensions
        doDeleteInstalled(extension.getExtensionId());

        getDataManager().update(new Extension.Builder().createFrom(extension)
            .status(Extension.Status.Installed)
            .lastUpdated(rightNow)
            .build());
    }

    @GET
    @Path(value = "/{id}/integrations")
    public Set<ResourceIdentifier> integrations(@NotNull @PathParam("id") final String id) {
        Extension extension = getDataManager().fetch(Extension.class, id);
        return integrations(extension);
    }

    @Override
    public Extension get(String id) {
        Extension extension = Getter.super.get(id);
        return enhance(extension);
    }

    @Override
    public ListResult<Extension> list(UriInfo uriInfo) {
        ListResult<Extension> extensions = Lister.super.list(uriInfo);

        List<Extension> enhanced = extensions.getItems().stream()
            .map(this::enhance)
            .collect(Collectors.toList());

        return new ListResult.Builder<Extension>()
            .items(enhanced)
            .totalCount(extensions.getTotalCount())
            .build();
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
        Date rightNow = new Date();
        getDataManager().update(new Extension.Builder()
            .createFrom(extension)
            .status(Extension.Status.Deleted)
            .lastUpdated(rightNow)
            .build());
    }

    private Set<ResourceIdentifier> integrations(Extension extension) {
        return getDataManager().fetchAll(Integration.class).getItems().stream()
            .filter(integration -> isIntegrationActiveAndUsingExtension(integration, extension))
            .map(this::toResourceIdentifier)
            .collect(Collectors.toSet());
    }

    private ResourceIdentifier toResourceIdentifier(Integration integration) {
        return new ResourceIdentifier.Builder()
            .id(integration.getId())
            .kind(integration.getKind())
            .name(Optional.ofNullable(integration.getName()))
            .build();
    }

    private boolean isIntegrationActiveAndUsingExtension(Integration integration, Extension extension) {
        if (integration == null || extension == null) {
            return false;
        }

        if (integration.getDesiredStatus().isPresent() && Integration.Status.Deleted.equals(integration.getDesiredStatus().get())) {
            return false;
        }

        return integration.getSteps().stream().anyMatch(step ->
            extension.getExtensionId().equals(
                Optional.ofNullable(step)
                .flatMap(Step::getExtension)
                .map(Extension::getExtensionId)
                .orElse(null)
            )
        );
    }

    private Extension enhance(Extension extension) {
        return new Extension.Builder()
            .createFrom(extension)
            .uses(integrations(extension).size())
            .build();
    }

}
