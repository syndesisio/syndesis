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
package io.syndesis.connector.apiprovider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.syndesis.common.util.Json;

public class ErrorStatusInfo {

    private Integer responseCode;
    private String category;
    private String message;

    private static final Logger LOG = LoggerFactory.getLogger(ErrorStatusInfo.class);

    public ErrorStatusInfo(Integer responseCode, String category, String message) {
        this.responseCode = responseCode;
        this.message = message;
        this.category = category;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String toJson() {
        try {
            return Json.writer().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            LOG.warn(e.getMessage());
            return "";
        }
    }
}
