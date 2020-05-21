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
package io.syndesis.dv.utils;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 *
 */
public final class Messages {
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
     * @return i18n string
     */
    @SuppressWarnings("PMD.AvoidCatchingNPE")
    public static String getString(Enum<?> key, ResourceBundle bundle) {
        String enumKey = key.getDeclaringClass().getSimpleName() + '.' + key.name();
        try {
            return bundle.getString(enumKey);
        } catch (NullPointerException ignored) {
            return "<No message available>";
        } catch (MissingResourceException ignored) {
            return "<Missing message for key \"" + enumKey + "\" in: " + BUNDLE_NAME + '>';
        } catch (final Exception err) {
            return err.getLocalizedMessage();
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
     * @return i18n string
     */
    public static String getString(Enum<?> key, Object... parameters) {
        return getString(key, RESOURCE_BUNDLE, parameters);
    }
}
