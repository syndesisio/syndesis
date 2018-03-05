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
package io.syndesis.server.dao.file;

import java.io.InputStream;

public interface FileDAO {

    /**
     * Write a file on a path.
     *
     * The path must be absolute (e.g. "/path/to/file.zip").
     *
     * If a file already exists it is overwritten.
     * Parent directories are created automatically.
     *
     * @param path the destination path
     * @param file the content of the file
     */
    void write(String path, InputStream file);

    /**
     * Write a file on a temporary path.
     *
     * The path wil be decided by the file store and returned to the client.
     *
     * @param file the content of the file
     * @return the path created for the file
     */
    String writeTemporaryFile(InputStream file);

    /**
     * Read a file from a path.
     *
     * The path must be absolute (e.g. "/path/to/file.zip").
     *
     * @param path the path to read
     * @return the file content or null if the file is not present
     */
    InputStream read(String path);

    /**
     * Delete a file corresponding to a path.
     *
     * The path must be absolute (e.g. "/path/to/file.zip").
     *
     * @param path the path to the file to delete
     * @return true if the file existed before deleting
     */
    boolean delete(String path);

    /**
     * Moves a file from a source path to a destination path.
     *
     * Both paths must be absolute (e.g. "/path/to/file.zip").
     *
     * If a file already exists in the destination path, it is overwritten.
     * If the source file does not exist, the operation is cancelled and the
     * destination file (if present) is left unchanged.
     *
     * @param fromPath the source path
     * @param toPath the destination path
     * @return true if the source file existed before moving it
     */
    boolean move(String fromPath, String toPath);
}
