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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enumeration of Database products we have tested, and for which we ship
 * drivers for. One caveat is the Oracle Driver which cannot be shipped due to
 * restrictions on its license.
 *
 * @since 09/11/17
 * @author kstam
 *
 */
public enum DbEnum {
    APACHE_DERBY("APACHE DERBY"),
    ORACLE("ORACLE"),
    POSTGRESQL("POSTGRESQL"),
    MYSQL("MYSQL"),
    TEIID_SERVER("TEIID SERVER"),
    STANDARD("STANDARD");

    private static final Logger LOG = LoggerFactory.getLogger(DbEnum.class);

    private final String name;

    DbEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static DbEnum fromName(final String dbProductName) {
        try {
            return valueOf(dbProductName.toUpperCase(Locale.US).replaceAll(" ", "_"));
        } catch (IllegalArgumentException e) {
            LOG.info(dbProductName + " -> DbEnum.STANDARD");
            return STANDARD;
        }
    }
}
