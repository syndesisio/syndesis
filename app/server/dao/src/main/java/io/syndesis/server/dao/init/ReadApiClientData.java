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
package io.syndesis.server.dao.init;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.syndesis.common.util.Json;
import io.syndesis.server.dao.manager.EncryptionComponent;
import io.syndesis.common.model.ModelData;

public class ReadApiClientData {

    private static final TypeReference<List<ModelData<?>>> MODEL_DATA_TYPE = new TypeReference<List<ModelData<?>>>(){};
    private static final Pattern PATTERN = Pattern.compile("\\@(.*?)\\@");
    private final EncryptionComponent encryptionComponent;

    public ReadApiClientData() {
        this(new EncryptionComponent(null));
    }

    public ReadApiClientData(EncryptionComponent encryptionComponent) {
        this.encryptionComponent = encryptionComponent;
    }

    /**
     *
     * @param fileName
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public List<ModelData<?>> readDataFromFile(String fileName) throws JsonParseException, JsonMappingException, IOException {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName)) {
            if (is==null) {
                throw new FileNotFoundException("Cannot find file " + fileName + " on classpath");
            }
            String jsonText = findAndReplaceTokens(from(is),System.getenv());
            return Json.reader().forType(MODEL_DATA_TYPE).readValue(jsonText);
        }
    }
    public List<ModelData<?>> readDataFromString(String jsonText) throws JsonParseException, JsonMappingException, IOException {
        String json = findAndReplaceTokens(jsonText,System.getenv());
        return Json.reader().forType(MODEL_DATA_TYPE).readValue(json);
    }
    /**
     * Reads the InputStream and returns a String containing all content from the InputStream.
     * @param is - InputStream that will be read.
     * @return String containing all content from the InputStream
     */
    public String from(InputStream is) {
        try (Scanner scanner = new Scanner(is, "UTF-8") ) {
            return scanner.useDelimiter("\\A").next();
        }
    }
    /**
     * Finds tokens surrounded by "@" signs (for example @POSTGRESQL_SAMPLEDB_PASSWORD@) and replaces them
     * with values from System.env if a value is set in the environment.
     *
     * @param jsonText - String containing tokens
     * @param env - containing tokens
     * @return String with tokens resolved from env
     */
    public String findAndReplaceTokens(String jsonText, Map<String,String> env) {
        Matcher m = PATTERN.matcher(jsonText);
        String json = jsonText;
        while(m.find()) {
            final String token = m.group(1).toUpperCase(Locale.US);
            String envKey = token;
            if( token.startsWith("ENC:") ) {
                envKey = EncryptionComponent.stripPrefix(token, "ENC:");
            }
            String value = env.get(envKey);
            if (value!=null) {
                if( token.startsWith("ENC:") ) {
                    value = encryptionComponent.encrypt(value);
                }
                json = jsonText.replaceAll("@" + token + "@", value);
            }
        }
        return json;
    }

}
