/*
 * Copyright 2016 Red Hat, Inc.
 * <p>
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */
package io.fabric8.funktion.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
    public static FunktionConfig load() throws IOException {
        return findFromFolder(new File("."));
    }


    protected static FunktionConfig loadFromFile(File file) throws IOException {
        LOG.debug("Parsing funktion configuration from: " + file.getName());
        try {
            FunktionConfig config = FunktionConfigs.parseFunktionConfig(file);
            return validateConfig(config, file);
        } catch (IOException e) {
            throw new IOException("Failed to parse funktion config: " + file + ". " + e, e);
        }
    }

    protected static FunktionConfig validateConfig(FunktionConfig config, File file) {
        List<FunktionRule> rules = config.getRules();
        if (rules.isEmpty()) {
            throw new IllegalStateException("No Funktion rules defined in file: " + file.getPath());
        }
        return config;
    }

    protected static FunktionConfig validateConfig(FunktionConfig config, URL url) {
        List<FunktionRule> rules = config.getRules();
        if (rules.isEmpty()) {
            throw new IllegalStateException("No Funktion rules defined in URL: " + url);
        }
        return config;
    }


    /**
     * Tries to find the configuration from the current directory or a parent folder.
     */
    public static FunktionConfig findFromFolder(File folder) throws IOException {
        if (folder.isDirectory()) {
            File file = new File(folder, FILE_NAME);
            if (file != null && file.exists() && file.isFile()) {
                return loadFromFile(file);
            }
            File parentFile = folder.getParentFile();
            if (parentFile != null) {
                return findFromFolder(parentFile);
            }
            FunktionConfig answer = tryFindConfigOnClassPath();
            if (answer != null) {
                return answer;
            }
            throw new IOException("Funktion configuration file does not exist: " + file.getPath());
        } else if (folder.isFile()) {
           return loadFromFile(folder);
        }
        FunktionConfig answer = tryFindConfigOnClassPath();
        if (answer != null) {
            return answer;
        }
        throw new IOException("Funktion configuration folder does not exist: " + folder.getPath());
    }

    protected static FunktionConfig tryFindConfigOnClassPath() throws IOException {
        URL url = FunktionConfigs.class.getClassLoader().getResource(FILE_NAME);
        if (url != null) {
            try {
                FunktionConfig config = parseFunktionConfig(url);
                return validateConfig(config, url);
            } catch (IOException e) {
                throw new IOException("Failed to parse funktion config: " + url + ". " + e, e);
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
        LOG.info("Loading Funktion rules from file: " + file);
        return parseYaml(file, FunktionConfig.class);
    }

    public static FunktionConfig parseFunktionConfig(InputStream input) throws IOException {
        return parseYaml(input, FunktionConfig.class);
    }

    public static FunktionConfig parseFunktionConfig(URL url) throws IOException {
        LOG.info("Loading Funktion rules from URL: " + url);
        return parseYaml(url, FunktionConfig.class);
    }

    public static FunktionConfig parseFunktionConfig(String yaml) throws IOException {
        return parseYaml(yaml, FunktionConfig.class);
    }

    private static <T> T parseYaml(File file, Class<T> clazz) throws IOException {
        ObjectMapper mapper = createObjectMapper();
        return mapper.readValue(file, clazz);
    }

    private static <T> T parseYaml(URL url, Class<T> clazz) throws IOException {
        ObjectMapper mapper = createObjectMapper();
        return mapper.readValue(url, clazz);
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
     * Saves the funktion.yml file to the given project directory
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
