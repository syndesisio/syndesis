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
package io.syndesis.dv;

/**
 * A Komodo error.
 */
public class KException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * @param message
     *        the error message (cannot be empty)
     */
    public KException( final String message ) {
        super(message);
    }

    /**
     * @param message
     *        the error message (cannot be empty)
     * @param cause
     *        the initial error (cannot be <code>null</code>)
     */
    public KException( final String message,
                       final Throwable cause ) {
        super(message, cause);
    }

    /**
     * @param cause
     *        the initial error (cannot be <code>null</code>)
     */
    public KException( final Throwable cause ) {
        super(cause);
    }

}
