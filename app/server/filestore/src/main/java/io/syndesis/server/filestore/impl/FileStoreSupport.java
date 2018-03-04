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
package io.syndesis.server.filestore.impl;

import io.syndesis.server.dao.DaoException;

import java.util.regex.Pattern;

/**
 * Utility methods for any FileStore
 */
public final class FileStoreSupport {

    /**
     * Characters commonly accepted in file names (any position)
     */
    private static final String COM_CHAR = "\\w-()\\[\\],";

    /**
     * Pattern for a file name or a directory name
     */
    private static final String FDIR_PATTERN = "[.]?([" + COM_CHAR + "][" + COM_CHAR + ".]*|[" + COM_CHAR + "][" + COM_CHAR + ". ]*[" + COM_CHAR + ".])";

    /**
     * Complete pattern of a path
     */
    private static final Pattern VALID_PATH_PATTERN = Pattern.compile("^/(" + FDIR_PATTERN + "/)*" + FDIR_PATTERN + "$");

    private FileStoreSupport() {
    }

    /**
     * Checks if the path is valid and throws an exception if it's not.
     * @param path the path to check
     * @throws DaoException if the path is not valid
     */
    public static void checkValidPath(String path) throws DaoException {
        if (!isValidPath(path)) {
            throw new DaoException("Invalid path: " + path);
        }
    }

    /**
     * Checks if the path is valid
     * @param path the path to check
     * @return true if the path is valid
     */
    public static boolean isValidPath(String path) {
        return VALID_PATH_PATTERN.matcher(path).matches();
    }

}
