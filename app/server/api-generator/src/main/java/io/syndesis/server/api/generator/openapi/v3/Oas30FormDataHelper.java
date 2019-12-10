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

package io.syndesis.server.api.generator.openapi.v3;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import io.apicurio.datamodels.openapi.v3.models.Oas30MediaType;
import io.apicurio.datamodels.openapi.v3.models.Oas30RequestBody;

/**
 * Helper deals with form data content such as multipart form data and form urlencoded data.
 */
final class Oas30FormDataHelper {

    private Oas30FormDataHelper() {
        // utility class
    }

    /**
     * Enum of known form data media types.
     */
    enum MediaType {
        FORM_URLENCODED("application/x-www-form-urlencoded"),
        MULTIPART_FORM_DATA("multipart/form-data");

        private final String mediaType;

        MediaType(String mediaType) {
            this.mediaType = mediaType;
        }

        public static boolean isFormDataMediaType(String type) {
            return Arrays.stream(values()).anyMatch(m -> m.mediaType.equals(type));
        }
    }

    /**
     * Positive when given request body has a media type defined that complies with form data types.
     * @param requestBody the request body maybe holding some form data content.
     * @return true when form data content is present.
     */
    static boolean hasFormDataBody(Oas30RequestBody requestBody) {
        if (requestBody == null || requestBody.content == null) {
            return false;
        }

        return requestBody.content.keySet()
            .stream()
            .anyMatch(Oas30FormDataHelper::isFormDataMediaType);
    }

    /**
     * Checks if given media type is a form data media type as defined in this helper via known media types.
     * @param mediaType the media type value.
     * @return true if media type complies to one of known form data media types.
     */
    static boolean isFormDataMediaType(String mediaType) {
        return MediaType.isFormDataMediaType(mediaType);
    }

    /**
     * Gets the media type from the given list of defined media types that is associated with a known form data media type
     * as defined in this helper. Empty if no compliant media type has been found.
     * @param content map of media types available.
     * @return media type representing a form data media type or empty.
     */
    static Optional<Oas30MediaType> getFormDataContent(Map<String, Oas30MediaType> content) {
        if (content == null) {
            return Optional.empty();
        }

        for (MediaType m : MediaType.values()) {
            if (content.containsKey(m.mediaType)) {
                return Optional.of(content.get(m.mediaType));
            }
        }

        return Optional.empty();
    }
}
