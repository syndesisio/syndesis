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
package io.syndesis.server.runtime;

import java.io.InputStream;

import org.skife.jdbi.v2.DBI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.syndesis.server.dao.file.FileDAO;
import io.syndesis.server.dao.file.IconDao;
import io.syndesis.server.filestore.impl.SqlFileStore;

/**
 * Creates and configures the file store
 */
@Configuration
public class FileStoreConfiguration {

    public static final String ICON_FILE_STORE_PATH = "/icon/";

    @Bean(initMethod = "init")
    @Autowired
    public SqlFileStore sqlFileStore(final DBI dbi) {
        return new SqlFileStore(dbi);
    }

    @Bean()
    @Autowired
    public FileDAO extensionFileStore(final SqlFileStore fileStore) {
        return new FileDAO(){
            @Override
            public void write(String path, InputStream file) {
                fileStore.write(path, file);
            }

            @Override
            public String writeTemporaryFile(InputStream file) {
                return  fileStore.writeTemporaryFile(file);
            }

            @Override
            public InputStream read(String path) {
                return  fileStore.read(path);
            }

            @Override
            public boolean delete(String path) {
                return fileStore.delete(path);
            }

            @Override
            public boolean move(String fromPath, String toPath) {
                return fileStore.move(fromPath, toPath);
            }
        };

    }

    @Bean()
    @Autowired
    public IconDao iconFileStore(final SqlFileStore fileStore) {
        return new IconDao() {

            @Override
            public void write(String id, InputStream iconContents) {
                fileStore.write(ICON_FILE_STORE_PATH + id, iconContents);
            }

            @Override
            public InputStream read(String id) {
                return fileStore.read(ICON_FILE_STORE_PATH + id);
            }

            @Override
            public boolean delete(String id) {
                return fileStore.delete(ICON_FILE_STORE_PATH + id);
            }
        };
    }
}
