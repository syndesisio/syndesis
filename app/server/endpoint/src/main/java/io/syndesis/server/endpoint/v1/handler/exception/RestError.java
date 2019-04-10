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
package io.syndesis.server.endpoint.v1.handler.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"errorCode", "userMsg", "developerMsg"})
@JsonDeserialize(
    using = JsonDeserializer.None.class
)
public class RestError {

    @JsonProperty("developerMsg")
    String developerMsg;

    @JsonProperty("userMsg")
    String userMsg;

    @JsonProperty("userMsgDetail")
    String userMsgDetail;

    @JsonProperty("errorCode")
    Integer errorCode;

    public RestError() {
        // makes it a Java bean
    }

    public RestError(String developerMsg, String userMsg,  String userMsgDetail, Integer errorCode) {
        this.developerMsg = developerMsg;
        this.userMsg = userMsg;
        this.userMsgDetail = userMsgDetail;
        this.errorCode = errorCode;
    }
    public String getDeveloperMsg() {
        return developerMsg;
    }
    public void setDeveloperMsg(String developerMsg) {
        this.developerMsg = developerMsg;
    }
    public String getUserMsg() {
        return userMsg;
    }
    public void setUserMsg(String userMsg) {
        this.userMsg = userMsg;
    }
    public String getUserMsgDetail() {
        return userMsgDetail;
    }
    public void setUserMsgDetail(String userMsgDetail) {
        this.userMsgDetail = userMsgDetail;
    }
    public Integer getErrorCode() {
        return errorCode;
    }
    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }
}
