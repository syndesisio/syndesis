/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.dao.init;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import io.syndesis.core.Json;

public class ReadApiClientData {

    /**
     *
     * @param fileName
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public List<ModelData> readDataFromFile(String fileName) throws JsonParseException, JsonMappingException, IOException {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
        if (is==null) {
            throw new FileNotFoundException("Cannot find file " + fileName + " on classpath");
        }
        return Json.mapper().readValue(is, new TypeReference<List<ModelData>>(){});
    }

}
