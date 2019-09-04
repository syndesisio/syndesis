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
package io.syndesis.common.util;

public class SyndesisConnectorException extends RuntimeException {

    private final String category;

    private static final long serialVersionUID = 3476018743129184217L;

    public SyndesisConnectorException() {
        this(ErrorCategory.SERVER_ERROR);
    }

    public SyndesisConnectorException(final String category) {
        this(category, null);
    }

    public SyndesisConnectorException(final String category, final String message) {
        this(category, message, null);
    }

    private SyndesisConnectorException(final String category, final String message, final Throwable cause) {
        super(message, cause);
        this.category = category!=null?category:ErrorCategory.SERVER_ERROR;
    }

    /**
     * Obtains the category.
     * @return
     */
    public String getCategory() {
        return category;
    }

    public static SyndesisConnectorException wrap(final String category, final Throwable cause) {
        return wrap(category, cause, null);
    }

    public static SyndesisConnectorException wrap(final String category, final Throwable cause, final String defaultMessage) {
        String detailedMsg = defaultMessage;
        if (cause != null && cause.getMessage() != null) {
            detailedMsg = cause.getMessage();
        }
        return new SyndesisConnectorException(category, detailedMsg, cause);
    }

    /**
     * Returns a SyndesysConnectorException
     * @param cause
     * @return SyndesysConnectorException
     */
    public static SyndesisConnectorException from(Throwable exception) {

        if (exception instanceof SyndesisConnectorException) {
            return (SyndesisConnectorException) exception;
        }
        if (exception.getCause() != null && exception.getCause() instanceof SyndesisConnectorException) {
            return (SyndesisConnectorException) exception.getCause();
        }
        if (exception.getCause() != null && exception.getCause().getMessage() != null) {
            return new SyndesisConnectorException(ErrorCategory.SERVER_ERROR,exception.getCause().getMessage(), exception.getCause());
        }
        if (exception.getMessage()!=null) {
            return new SyndesisConnectorException(ErrorCategory.SERVER_ERROR, exception.getMessage(), exception);
        }
        if (exception.getCause() != null) {
            return new SyndesisConnectorException(ErrorCategory.SERVER_ERROR,exception.getCause().getMessage(), exception.getCause());
        }
        return new SyndesisConnectorException(ErrorCategory.SERVER_ERROR);
    }
}
