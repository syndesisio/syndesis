/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fabric8.funktion.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.fabric8.funktion.support.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class FunktionConfigs {
    private static final transient Logger LOG = LoggerFactory.getLogger(FunktionConfigs.class);

    public static final String FILE_NAME = "funktion.yml";

    protected static String toYaml(Object dto) throws JsonProcessingException {
        ObjectMapper mapper = createObjectMapper();
        return mapper.writeValueAsString(dto);
    }

    /**
     * Tries to load the configuration file from the current directory
     */
    public static FunktionConfig load() {
        return loadFromFolder(new File("."));
    }


    /**
     * Returns the configuration from the {@link #FILE_NAME} in the given folder or returns the default configuration
     */
    public static FunktionConfig loadFromFolder(File folder) {
        File FunktionConfigFile = new File(folder, FILE_NAME);
        if (FunktionConfigFile != null && FunktionConfigFile.exists() && FunktionConfigFile.isFile()) {
            LOG.debug("Parsing funktion configuration from: " + FunktionConfigFile.getName());
            try {
                return FunktionConfigs.parseFunktionConfig(FunktionConfigFile);
            } catch (IOException e) {
                LOG.warn("Failed to parse " + FunktionConfigFile + ". " + e, e);
            }
        }
        return new FunktionConfig();
    }


    /**
     * Tries to find the project configuration from the current directory or a parent folder.
     * <p>
     * If no fabric8.yml file can be found just return an empty configuration
     */
    public static FunktionConfig findFromFolder(File folder) {
        if (folder.isDirectory()) {
            File FunktionConfigFile = new File(folder, FILE_NAME);
            if (FunktionConfigFile != null && FunktionConfigFile.exists() && FunktionConfigFile.isFile()) {
                return loadFromFolder(folder);
            }
            File parentFile = folder.getParentFile();
            if (parentFile != null) {
                return findFromFolder(parentFile);
            }
        }
        return new FunktionConfig();
    }

    /**
     * Returns the project config from the given url if it exists or null
     */
    public static FunktionConfig loadFromUrl(String url) {
        if (!Strings.isEmpty(url)) {
            try {
                return loadFromUrl(new URL(url));
            } catch (MalformedURLException e) {
                LOG.warn("Failed to create URL from: " + url + ". " + e, e);
            }
        }
        return null;
    }

    /**
     * Returns the config from the given url if it exists or null
     */
    public static FunktionConfig loadFromUrl(URL url) {
        InputStream input = null;
        try {
            input = url.openStream();
        } catch (FileNotFoundException e) {
            LOG.info("No fabric8.yml at URL: " + url);
        } catch (IOException e) {
            LOG.warn("Failed to open fabric8.yml file at URL: " + url + ". " + e, e);
        }
        if (input != null) {
            try {
                LOG.info("Parsing " + FunktionConfigs.FILE_NAME + " from " + url);
                return FunktionConfigs.parseFunktionConfig(input);
            } catch (IOException e) {
                LOG.warn("Failed to parse " + FunktionConfigs.FILE_NAME + " from " + url + ". " + e, e);
            }
        }
        return null;
    }

    /**
     * Returns true if the given folder has a configuration file called {@link #FILE_NAME}
     */
    public static boolean hasConfigFile(File folder) {
        File FunktionConfigFile = new File(folder, FILE_NAME);
        return FunktionConfigFile != null && FunktionConfigFile.exists() && FunktionConfigFile.isFile();
    }

    /**
     * Creates a configured Jackson object mapper for parsing YAML
     */
    public static ObjectMapper createObjectMapper() {
        return new ObjectMapper(new YAMLFactory());
    }

    public static FunktionConfig parseFunktionConfig(File file) throws IOException {
        return parseYaml(file, FunktionConfig.class);
    }

    public static FunktionConfig parseFunktionConfig(InputStream input) throws IOException {
        return parseYaml(input, FunktionConfig.class);
    }

    public static FunktionConfig parseFunktionConfig(String yaml) throws IOException {
        return parseYaml(yaml, FunktionConfig.class);
    }

    private static <T> T parseYaml(File file, Class<T> clazz) throws IOException {
        ObjectMapper mapper = createObjectMapper();
        return mapper.readValue(file, clazz);
    }

    static <T> List<T> parseYamlValues(File file, Class<T> clazz) throws IOException {
        ObjectMapper mapper = createObjectMapper();
        MappingIterator<T> iter = mapper.readerFor(clazz).readValues(file);
        List<T> answer = new ArrayList<>();
        while (iter.hasNext()) {
            answer.add(iter.next());
        }
        return answer;
    }

    private static <T> T parseYaml(InputStream inputStream, Class<T> clazz) throws IOException {
        ObjectMapper mapper = createObjectMapper();
        return mapper.readValue(inputStream, clazz);
    }

    private static <T> T parseYaml(String yaml, Class<T> clazz) throws IOException {
        ObjectMapper mapper = createObjectMapper();
        return mapper.readValue(yaml, clazz);
    }

    /**
     * Saves the fabric8.yml file to the given project directory
     */
    public static boolean saveToFolder(File basedir, FunktionConfig config, boolean overwriteIfExists) throws IOException {
        File file = new File(basedir, FunktionConfigs.FILE_NAME);
        if (file.exists()) {
            if (!overwriteIfExists) {
                LOG.warn("Not generating " + file + " as it already exists");
                return false;
            }
        }
        return saveConfig(config, file);
    }

    /**
     * Saves the configuration as YAML in the given file
     */
    public static boolean saveConfig(FunktionConfig config, File file) throws IOException {
        createObjectMapper().writeValue(file, config);
        return true;
    }

}
