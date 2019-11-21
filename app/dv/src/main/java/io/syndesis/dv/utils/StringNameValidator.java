/*
 * Copyright (C) 2013 Red Hat, Inc.
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
package io.syndesis.dv.utils;

import org.teiid.core.util.ArgCheck;

/**
 * StringNameValidator
 *
 *
 */
public class StringNameValidator {

    public static final int MAXIMUM_LENGTH = Integer.MAX_VALUE;
    public static final int DEFAULT_MAXIMUM_LENGTH = 255;
    public static final int DEFAULT_MINIMUM_LENGTH = 1;
    public static final boolean DEFAULT_CASE_SENSITIVE_NAME_COMPARISON = false;
    private final int maximumLength;
    private final int minimumLength;

    /**
     * Construct an instance of StringNameValidator.
     */
    public StringNameValidator( final int minLength,
                                final int maxLength) {
        super();
        this.minimumLength = minLength < 0 ? DEFAULT_MINIMUM_LENGTH : minLength;
        this.maximumLength = maxLength < 0 ? MAXIMUM_LENGTH : maxLength;
        if (this.minimumLength > this.maximumLength) {
            final String msg = Messages.getString(Messages.StringNameValidator.minLengthNotExceedMaxLength);
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Construct an instance of StringNameValidator.
     */
    public StringNameValidator() {
        this(DEFAULT_MINIMUM_LENGTH, DEFAULT_MAXIMUM_LENGTH);
    }

    /**
     * @return
     */
    public int getMaximumLength() {
        return maximumLength;
    }

    /**
     * @return
     */
    public int getMinimumLength() {
        return minimumLength;
    }

    /**
     * Check whether the name length is between {@link #getMinimumLength()} and {@link #getMaximumLength()} (inclusive).
     *
     * @param name the name to check; may not be null
     * @return a message stating what is wrong with the name, or null if the name is considered valid
     */
    public String checkNameLength( final String name ) {
        ArgCheck.isNotNull(name);
        final int strLength = name.length();
        if (strLength < getMinimumLength()) {
            final Object[] params = new Object[] {new Integer(getMinimumLength())};
            final String msg = Messages.getString(Messages.StringNameValidator.minLengthFailure, params);
            return msg;
            // check the entity name length against a desired value
        } else if (strLength > getMaximumLength()) {
            final Object[] params = new Object[] {new Integer(strLength), new Integer(getMaximumLength())};
            final String msg = Messages.getString(Messages.StringNameValidator.nameLengthLongerThanAllowed, params);
            return msg;
        }

        // Valid, so return no error message
        return null;
    }

    public String checkValidName( final String name ) {
        // The name may not be null
        if (name == null) {
            final String msg = Messages.getString(Messages.StringNameValidator.nameNotNull);
            return msg;
        }

        // Check the length of the name ...
        // the length is being separately checked by a different method
        // which is invoked by String length rule, need not be checked twice.
        final String lengthMsg = checkNameLength(name);
        if (lengthMsg != null) {
            return lengthMsg;
        }

        // If it passed all of the tests ...
        return null;
    }

}
