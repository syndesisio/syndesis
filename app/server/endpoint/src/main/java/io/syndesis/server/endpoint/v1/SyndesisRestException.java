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
package io.syndesis.server.endpoint.v1;

/**
 * A fully customizable rest exception.
 */
public class SyndesisRestException extends RuntimeException {

    private final String developerMsg;

    private final String userMsg;

    private final String userMsgDetail;

    private final Integer errorCode;

    public SyndesisRestException(String developerMsg, String userMsg, String userMsgDetail, Integer errorCode) {
        super(developerMsg);
        this.developerMsg = developerMsg;
        this.userMsg = userMsg;
        this.userMsgDetail = userMsgDetail;
        this.errorCode = errorCode;
    }

    public SyndesisRestException(String developerMsg, String userMsg, String userMsgDetail, Integer errorCode, Throwable cause) {
        super(developerMsg, cause);
        this.developerMsg = developerMsg;
        this.userMsg = userMsg;
        this.userMsgDetail = userMsgDetail;
        this.errorCode = errorCode;
    }

    public String getDeveloperMsg() {
        return developerMsg;
    }

    public String getUserMsg() {
        return userMsg;
    }

    public String getUserMsgDetail() {
        return userMsgDetail;
    }

    public Integer getErrorCode() {
        return errorCode;
    }
}
