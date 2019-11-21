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

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import io.syndesis.dv.StringConstants;

/**
 *
 */
public class Messages implements StringConstants {
    private static final String BUNDLE_NAME = "io.syndesis.dv.utils.messages"; //$NON-NLS-1$

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    @SuppressWarnings( "javadoc" )
    public enum StringNameValidator {
        nameNotNull,
        nameLengthLongerThanAllowed,
        minLengthFailure,
        minLengthNotExceedMaxLength
    }

    private Messages() {
    }

    /**
     * Get message string
     *
     * @param key
     *
     * @return i18n string
     */
    public static String getString(Enum<?> key, ResourceBundle bundle) {
        String enumKey = key.getClass().getSimpleName() + '.' + key.name();
        try {
            return bundle.getString(enumKey);
        } catch (final Exception err) {
            String msg;

            if (err instanceof NullPointerException) {
                msg = "<No message available>"; //$NON-NLS-1$
            } else if (err instanceof MissingResourceException) {
                msg = "<Missing message for key \"" + enumKey + "\" in: " + BUNDLE_NAME + '>'; //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                msg = err.getLocalizedMessage();
            }

            return msg;
        }
    }

    public static String getString(Enum<?> key, ResourceBundle resourceBundle, Object... parameters) {
        String text = getString(key, resourceBundle);

        if (parameters == null || parameters.length == 0) {
            return text;
        }

        return String.format( text, parameters );
    }

    /**
     * Get message string with parameters
     *
     * @param key
     * @param parameters
     *
     * @return i18n string
     */
    public static String getString(Enum<?> key, Object... parameters) {
        return getString(key, RESOURCE_BUNDLE, parameters);
    }
}
