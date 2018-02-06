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
package io.syndesis.runtime;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import com.fasterxml.jackson.databind.JsonNode;
import io.syndesis.core.Json;
import io.syndesis.extension.converter.ExtensionConverter;
import io.syndesis.model.ListResult;
import io.syndesis.model.ResourceIdentifier;
import io.syndesis.model.Violation;
import io.syndesis.model.extension.Extension;
import io.syndesis.model.integration.IntegrationDeployment;
import io.syndesis.model.integration.IntegrationDeploymentSpec;
import io.syndesis.model.integration.IntegrationDeploymentState;
import io.syndesis.model.integration.Step;
import io.syndesis.rest.v1.handler.exception.RestError;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

public class ExtensionsITCase extends BaseITCase {

    @Test
    public void basicConnectivityTest() {
        ResponseEntity<ListResult<Extension>> exts = get("/api/v1/extensions",
            new ParameterizedTypeReference<ListResult<Extension>>() {}, tokenRule.validToken(), HttpStatus.OK);
        assertThat(exts.getBody().getTotalCount()).isGreaterThanOrEqualTo(0);
    }

    @Test
    public void createNewExtensionListDeleteTest() throws IOException {
        // POST
        ResponseEntity<Extension> created = post("/api/v1/extensions", multipartBody(extensionData(1)),
            Extension.class, tokenRule.validToken(), HttpStatus.OK, multipartHeaders());

        assertThat(created.getBody().getId()).isNotEmpty();
        assertThat(created.getBody().getName()).isNotBlank();
        assertThat(created.getBody().getCreatedDate()).isNotEmpty();
        assertThat(created.getBody().getLastUpdated()).isNotEmpty();
        assertThat(created.getBody().getUserId()).contains("someone_important");

        assertThat(created.getBody().getId()).isPresent();
        String id = created.getBody().getId().get();

        // GET
        ResponseEntity<Extension> got = get("/api/v1/extensions/" + id, Extension.class,
            tokenRule.validToken(), HttpStatus.OK);

        assertThat(got.getBody().getName()).isEqualTo(created.getBody().getName());

        // LIST
        ResponseEntity<ListResult<Extension>> list = get("/api/v1/extensions?query=status=" + Extension.Status.Draft,
            new ParameterizedTypeReference<ListResult<Extension>>() {}, tokenRule.validToken(), HttpStatus.OK);

        assertThat(list.getBody().getTotalCount()).as("extensions size").isGreaterThan(0);

        // DELETE
        delete("/api/v1/extensions/" + id, Void.class, tokenRule.validToken(), HttpStatus.NO_CONTENT);

        // RE-GET
        ResponseEntity<Extension> regot = get("/api/v1/extensions/" + id, Extension.class,
            tokenRule.validToken(), HttpStatus.OK);

        assertThat(regot.getBody().getStatus()).contains(Extension.Status.Deleted);
    }

    @Test
    public void testUpdateExtension() throws IOException {
        ResponseEntity<Extension> created = post("/api/v1/extensions", multipartBody(extensionData(1)),
            Extension.class, tokenRule.validToken(), HttpStatus.OK, multipartHeaders());

        assertThat(created.getBody().getId()).isPresent();
        String id = created.getBody().getId().get();

        ResponseEntity<Extension> updated = post("/api/v1/extensions?updatedId=" + id, multipartBody(extensionData(1)),
            Extension.class, tokenRule.validToken(), HttpStatus.OK, multipartHeaders());

        assertThat(updated.getBody().getId()).isPresent();
    }

    @Test
    public void testUpdateExtensionFailure() throws IOException {
        ResponseEntity<Extension> created = post("/api/v1/extensions", multipartBody(extensionData(1)),
            Extension.class, tokenRule.validToken(), HttpStatus.OK, multipartHeaders());

        assertThat(created.getBody().getId()).isPresent();
        String id = created.getBody().getId().get();

        // Using wrong extensionId="extension2"
        ResponseEntity<RestError> res = post("/api/v1/extensions?updatedId=" + id, multipartBody(extensionData(2)),
            RestError.class, tokenRule.validToken(), HttpStatus.BAD_REQUEST, multipartHeaders());

        assertThat(res.getBody().getUserMsg()).startsWith("The uploaded extensionId");
    }

    @Test
    public void testReimportExtensionFailure() throws IOException {
        ResponseEntity<Extension> created = post("/api/v1/extensions", multipartBody(extensionData(1)),
            Extension.class, tokenRule.validToken(), HttpStatus.OK, multipartHeaders());

        assertThat(created.getBody().getId()).isPresent();
        String id = created.getBody().getId().get();

        // Install it
        post("/api/v1/extensions/" + id + "/install", null, Void.class,
            tokenRule.validToken(), HttpStatus.NO_CONTENT);

        // Using same extension without setting extensionId
        post("/api/v1/extensions", multipartBody(extensionData(1)),
            Void.class, tokenRule.validToken(), HttpStatus.BAD_REQUEST, multipartHeaders());
    }

    @Test
    public void testValidateExtension() throws IOException {
        // Create one extension
        ResponseEntity<Extension> created1 = post("/api/v1/extensions", multipartBody(extensionData(1)),
            Extension.class, tokenRule.validToken(), HttpStatus.OK, multipartHeaders());

        assertThat(created1.getBody().getId()).isPresent();
        String id1 = created1.getBody().getId().get();

        // Install it
        post("/api/v1/extensions/" + id1 + "/install", null, Void.class,
            tokenRule.validToken(), HttpStatus.NO_CONTENT);

        // Create another extension with same extension-id
        ResponseEntity<Extension> created2 = post("/api/v1/extensions?updatedId=" + id1, multipartBody(extensionData(1)),
            Extension.class, tokenRule.validToken(), HttpStatus.OK, multipartHeaders());

        assertThat(created2.getBody().getId()).isPresent();
        String id2 = created2.getBody().getId().get();

        // 200 status code: it's just a warning
        ResponseEntity<Set<Violation>> violations = post("/api/v1/extensions/" + id2 + "/validation",
            null, new ParameterizedTypeReference<Set<Violation>>() {}, tokenRule.validToken(), HttpStatus.OK);

        assertThat(violations.getBody().size()).isGreaterThan(0);
        assertThat(violations.getBody())
            .hasOnlyOneElementSatisfying(v -> assertThat(v.message()).startsWith("The tech extension already exists"));
    }

    @Test
    public void testExtensionActivation() throws IOException {
        // Create one extension
        ResponseEntity<Extension> created1 = post("/api/v1/extensions", multipartBody(extensionData(1)),
            Extension.class, tokenRule.validToken(), HttpStatus.OK, multipartHeaders());

        assertThat(created1.getBody().getId()).isPresent();
        String id1 = created1.getBody().getId().get();

        // Create another extension (id-2)
        ResponseEntity<Extension> created2 = post("/api/v1/extensions", multipartBody(extensionData(2)),
            Extension.class, tokenRule.validToken(), HttpStatus.OK, multipartHeaders());

        assertThat(created2.getBody().getId()).isPresent();
        String id2 = created2.getBody().getId().get();

        // Install them
        post("/api/v1/extensions/" + id1 + "/install", null, Void.class,
            tokenRule.validToken(), HttpStatus.NO_CONTENT);

        post("/api/v1/extensions/" + id2 + "/install", null, Void.class,
            tokenRule.validToken(), HttpStatus.NO_CONTENT);

        // Check status 1
        ResponseEntity<Extension> got1 = get("/api/v1/extensions/" + id1, Extension.class,
            tokenRule.validToken(), HttpStatus.OK);
        assertThat(got1.getBody().getStatus()).contains(Extension.Status.Installed);

        // Check status 2
        ResponseEntity<Extension> got2 = get("/api/v1/extensions/" + id2, Extension.class,
            tokenRule.validToken(), HttpStatus.OK);
        assertThat(got2.getBody().getStatus()).contains(Extension.Status.Installed);

        // Create another extension with same extension-id
        ResponseEntity<Extension> createdCopy1 = post("/api/v1/extensions?updatedId=" + id1, multipartBody(extensionData(1)),
            Extension.class, tokenRule.validToken(), HttpStatus.OK, multipartHeaders());

        assertThat(createdCopy1.getBody().getId()).isPresent();
        String idCopy1 = createdCopy1.getBody().getId().get();

        // Install copyObjectMapperConfiguration
        post("/api/v1/extensions/" + idCopy1 + "/install", null, Void.class,
            tokenRule.validToken(), HttpStatus.NO_CONTENT);

        // Check previous extension is deleted
        ResponseEntity<Extension> reGot1 = get("/api/v1/extensions/" + id1, Extension.class,
            tokenRule.validToken(), HttpStatus.OK);
        assertThat(reGot1.getBody().getStatus()).contains(Extension.Status.Deleted);

        // Check new extension is installed
        ResponseEntity<Extension> gotCopy1 = get("/api/v1/extensions/" + idCopy1, Extension.class,
            tokenRule.validToken(), HttpStatus.OK);
        assertThat(gotCopy1.getBody().getStatus()).contains(Extension.Status.Installed);

        // Check 2nd extension is unchanged
        ResponseEntity<Extension> reGot2 = get("/api/v1/extensions/" + id2, Extension.class,
            tokenRule.validToken(), HttpStatus.OK);
        assertThat(reGot2.getBody().getStatus()).contains(Extension.Status.Installed);
    }

    @Test
    public void testIntegrationsUsingExtension() throws IOException {
        // Create one extension
        ResponseEntity<Extension> created = post("/api/v1/extensions", multipartBody(extensionData(1)),
            Extension.class, tokenRule.validToken(), HttpStatus.OK, multipartHeaders());

        assertThat(created.getBody().getId()).isPresent();
        String id = created.getBody().getId().get();

        // Get extensions using it
        ResponseEntity<Set<ResourceIdentifier>> got1 = get("/api/v1/extensions/" + id + "/integrations",
            new ParameterizedTypeReference<Set<ResourceIdentifier>>() {}, tokenRule.validToken(), HttpStatus.OK);

        assertThat(got1.getBody()).isEmpty();

        dataManager.create(new IntegrationDeployment.Builder()
            .integrationId("integration-extension-1")
            .version(1)
            .targetState(IntegrationDeploymentState.Active)
            .currentState(IntegrationDeploymentState.Active)
            .createdDate(new Date())
            .lastUpdated(new Date())
            .spec(new IntegrationDeploymentSpec.Builder()
            .steps(Collections.singletonList(
                new Step.Builder()
                    .id("step1")
                    .name("step1")
                    .stepKind("extension")
                    .extension(
                        new Extension.Builder()
                            .createFrom(created.getBody())
                            .build())
                    .build())).build())
            .build());

        // Create a inactive integration that uses the extension
        dataManager.create(new IntegrationDeployment.Builder()
            .integrationId("integration-extension-2")
            .version(1)
            .targetState(IntegrationDeploymentState.Undeployed)
            .currentState(IntegrationDeploymentState.Active)
            .createdDate(new Date())
            .lastUpdated(new Date())
            .spec(new IntegrationDeploymentSpec.Builder()
            .steps(Collections.singletonList(
                new Step.Builder()
                    .id("step1")
                    .name("step1")
                    .stepKind("extension")
                    .extension(
                        new Extension.Builder()
                            .createFrom(created.getBody())
                            .build())
                    .build())).build())
            .build());

        // Get extensions using it
        ResponseEntity<Set<ResourceIdentifier>> got2 = get("/api/v1/extensions/" + id + "/integrations",
            new ParameterizedTypeReference<Set<ResourceIdentifier>>() {}, tokenRule.validToken(), HttpStatus.OK);

        assertThat(got2.getBody().size()).isEqualTo(1);
        assertThat(got2.getBody()).allMatch(ri -> ri.getId().isPresent() && ri.getId().get().equals("integration-extension-1"));

        dataManager.delete(IntegrationDeployment.class, "integration-extension-1");
        dataManager.delete(IntegrationDeployment.class, "integration-extension-2");
    }

    @Test
    public void testListExtensionDetails() throws IOException {
        // Create one extension
        ResponseEntity<Extension> created = post("/api/v1/extensions", multipartBody(extensionData(1)),
            Extension.class, tokenRule.validToken(), HttpStatus.OK, multipartHeaders());

        assertThat(created.getBody().getId()).isPresent();
        String id = created.getBody().getId().get();

        // Install it
        post("/api/v1/extensions/" + id + "/install", null, Void.class,
            tokenRule.validToken(), HttpStatus.NO_CONTENT);

        // Get extension details
        ResponseEntity<Extension> got1 = get("/api/v1/extensions/" + id,
            Extension.class, tokenRule.validToken(), HttpStatus.OK);

        assertThat(got1.getBody().getUses()).hasValue(0);

        // Create a active integration that uses the extension
        dataManager.create(new IntegrationDeployment.Builder()
            .integrationId("integration-extension")
            .version(1)
            .targetState(IntegrationDeploymentState.Active)
            .currentState(IntegrationDeploymentState.Active)
            .createdDate(new Date())
            .lastUpdated(new Date())
            .spec(new IntegrationDeploymentSpec.Builder()
            //.userId("important user")
            .steps(Collections.singletonList(
                new Step.Builder()
                    .id("step1")
                    .name("step1")
                    .stepKind("extension")
                    .extension(
                        new Extension.Builder()
                            .createFrom(created.getBody())
                            .build())
                    .build())).build())
            .build());


        // Get extension details again
        ResponseEntity<Extension> got2 = get("/api/v1/extensions/" + id,
            Extension.class, tokenRule.validToken(), HttpStatus.OK);


        assertThat(got2.getBody().getUses()).hasValue(1);

        // Get extension list
        ResponseEntity<ListResult<Extension>> list = get("/api/v1/extensions",
            new ParameterizedTypeReference<ListResult<Extension>>() {}, tokenRule.validToken(), HttpStatus.OK);

        assertThat(list.getBody().getItems()).hasSize(1);

        dataManager.delete(IntegrationDeployment.class, "integration-extension:1");
    }


    // ===========================================================

    private byte[] extensionData(int prg) throws IOException {
        try (ByteArrayOutputStream data = new ByteArrayOutputStream();
             JarOutputStream jar = new JarOutputStream(data)) {

            JarEntry definition = new JarEntry("META-INF/syndesis/syndesis-extension-definition.json");
            jar.putNextEntry(definition);

            Extension extension = new Extension.Builder()
                .schemaVersion(ExtensionConverter.getCurrentSchemaVersion())
                .extensionId("com.company:extension" + prg)
                .name("Extension " + prg)
                .description("Extension Description " + prg)
                .version("1.0")
                .extensionType(Extension.Type.Steps)
                .build();

            JsonNode extensionTree = ExtensionConverter.getDefault().toPublicExtension(extension);
            byte[] content = Json.writer().writeValueAsBytes(extensionTree);
            IOUtils.write(content, jar);
            jar.closeEntry();
            jar.flush();
            return data.toByteArray();
        }
    }

    private HttpHeaders multipartHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return headers;
    }

    private MultiValueMap<String, Object> multipartBody(byte[] data) {
        LinkedMultiValueMap<String, Object> multipartData = new LinkedMultiValueMap<>();
        multipartData.add("file", new InputStreamResource(new ByteArrayInputStream(data)));
        return multipartData;
    }

}
