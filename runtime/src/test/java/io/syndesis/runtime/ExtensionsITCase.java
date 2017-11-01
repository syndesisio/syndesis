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
package io.syndesis.runtime;

import io.syndesis.model.ListResult;
import io.syndesis.model.extension.Extension;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class ExtensionsITCase extends BaseITCase {

    @Test
    public void basicConnectivityTest() {
        ResponseEntity<ListResult<Extension>> exts = get("/api/v1beta1/extensions",
            new ParameterizedTypeReference<ListResult<Extension>>() {}, tokenRule.validToken(), HttpStatus.OK);
        assertThat(exts.getBody().getTotalCount()).isGreaterThanOrEqualTo(0);
    }

    @Test
    public void createNewExtensionListDeleteTest() {
        // Dummy data: replace with actual data when the binary format is defined
        byte[] data = {1, 2, 3, 4};

        // POST
        ResponseEntity<Extension> created = post("/api/v1beta1/extensions", multipartBody(data), Extension.class,
            tokenRule.validToken(), HttpStatus.OK, multipartHeaders());

        assertThat(created.getBody().getId()).isNotEmpty();
        assertThat(created.getBody().getName()).isNotBlank();

        String id = created.getBody().getId().get();

        // GET
        ResponseEntity<Extension> got = get("/api/v1beta1/extensions/" + id, Extension.class,
            tokenRule.validToken(), HttpStatus.OK);

        assertThat(got.getBody().getName()).isEqualTo(created.getBody().getName());

        // LIST
        ResponseEntity<ListResult<Extension>> list = get("/api/v1beta1/extensions",
            new ParameterizedTypeReference<ListResult<Extension>>() {}, tokenRule.validToken(), HttpStatus.OK);

        assertThat(list.getBody().getTotalCount()).as("extensions size").isGreaterThan(0);

        // DELETE
        delete("/api/v1beta1/extensions/" + id, Void.class, tokenRule.validToken(), HttpStatus.NO_CONTENT);

        // RE-GET
        get("/api/v1beta1/extensions/" + id, Void.class,
            tokenRule.validToken(), HttpStatus.NOT_FOUND);
    }

    // ===========================================================

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
