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
package com.redhat.ipaas.github.extended;

import java.util.Base64;

/**
 * Used for JSON serialization of a file content entry
 */
class FileContent {

    private String content;
    private String path;
    private String message;
    private String sha;

    FileContent() {}

    FileContent(String path, String message, byte[] content, String sha) {
        this.path = assertNotNull("path", path);
        this.content = assertNotNull("content", toBase64(content));
        this.message = assertNotNull("message", message);
        // Can be null for updates
        this.sha = sha;
    }

    private String assertNotNull(String what, String value) {
        if (value == null) {
            throw new IllegalArgumentException("No '" + what + "' given for file content request");
        }
        return value;
    }

    private String toBase64(byte[] content) {
        if (content == null) {
            return null;
        }
        return new String(Base64.getEncoder().encode(content));
    }

    public String getContent() {
        return content;
    }

    public String getPath() {
        return path;
    }

    public String getMessage() {
        return message;
    }

    public String getSha() {
        return sha;
    }
}
