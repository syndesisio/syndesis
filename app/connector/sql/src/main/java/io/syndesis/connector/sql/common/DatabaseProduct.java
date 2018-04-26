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
package io.syndesis.connector.sql.common;

import java.util.Locale;

/**
 * Enumeration of Database products we have tested, and for which we ship
 * drivers for. One caviat is the Oracle Driver which cannot be shipped due to
 * restrictions on its license.
 *
 * @since 09/11/17
 * @author kstam
 *
 */
public enum DatabaseProduct {
    APACHE_DERBY, ORACLE, POSTGRESQL, MYSQL;

    /**
     * Can be used to convert '_' to ' ' in the enum name.
     *
     * @return name of the enum.
     */
    public String nameWithSpaces() {
        return name().replaceAll("_", " ");
    }

    public static DatabaseProduct fromName(String databaseProductName) {
        return valueOf(databaseProductName.toUpperCase(Locale.US).replaceAll(" ", "_"));
    }
}
