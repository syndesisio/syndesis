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
package io.syndesis.integration.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SyndesisHelpers {
    public static final String FILE_NAME = "syndesis.yml";
    private static final transient Logger LOG = LoggerFactory.getLogger(SyndesisHelpers.class);

    protected static String toYaml(Object dto) throws JsonProcessingException {
        ObjectMapper mapper = createObjectMapper();
        return mapper.writeValueAsString(dto);
    }

    /**
     * Tries to load the configuration file from the current directory
     */
    public static SyndesisModel load() throws IOException {
        return findFromFolder(new File("."));
    }


    protected static SyndesisModel loadFromFile(File file) throws IOException {
        LOG.debug("Parsing syndesis configuration from: " + file.getName());
        try {
            SyndesisModel config = SyndesisHelpers.parseSyndesisConfig(file);
            return validateConfig(config, file);
        } catch (IOException e) {
            throw new IOException("Failed to parse syndesis config: " + file + ". " + e, e);
        }
    }


    public static SyndesisModel loadFromString(String yaml) throws IOException {
        return parseSyndesisConfig(yaml);
    }

    public static SyndesisModel loadFromURL(URL resource) throws IOException {
        return parseSyndesisConfig(resource);
    }

    protected static SyndesisModel validateConfig(SyndesisModel config, File file) {
        List<Flow> flows = config.getFlows();
        if (flows.isEmpty()) {
            throw new IllegalStateException("No SyndesisModel flows defined in file: " + file.getPath());
        }
        return config;
    }

    protected static SyndesisModel validateConfig(SyndesisModel config, URL url) {
        List<Flow> flows = config.getFlows();
        if (flows.isEmpty()) {
            throw new IllegalStateException("No SyndesisModel flows defined in URL: " + url);
        }
        return config;
    }


    /**
     * Tries to find the configuration from the current directory or a parent folder.
     */
    public static SyndesisModel findFromFolder(File folder) throws IOException {
        if (folder.isDirectory()) {
            File file = new File(folder, FILE_NAME);
            if (file != null && file.exists() && file.isFile()) {
                return loadFromFile(file);
            }
            File configFolder = new File(folder, "config");
            file = new File(configFolder, FILE_NAME);
            if (file != null && file.exists() && file.isFile()) {
                return loadFromFile(file);
            }
            File parentFile = folder.getParentFile();
            if (parentFile != null) {
                return findFromFolder(parentFile);
            }
            SyndesisModel answer = tryFindConfigOnClassPath();
            if (answer != null) {
                return answer;
            }
            throw new IOException("SyndesisModel configuration file does not exist: " + file.getPath());
        } else if (folder.isFile()) {
            return loadFromFile(folder);
        }
        SyndesisModel answer = tryFindConfigOnClassPath();
        if (answer != null) {
            return answer;
        }
        throw new IOException("SyndesisModel configuration folder does not exist: " + folder.getPath());
    }

    protected static SyndesisModel tryFindConfigOnClassPath() throws IOException {
        URL url = SyndesisHelpers.class.getClassLoader().getResource(FILE_NAME);
        if (url != null) {
            try {
                SyndesisModel config = parseSyndesisConfig(url);
                return validateConfig(config, url);
            } catch (IOException e) {
                throw new IOException("Failed to parse syndesis config: " + url + ". " + e, e);
            }
        }
        return null;
    }

    /**
     * Returns true if the given folder has a configuration file called {@link #FILE_NAME}
     */
    public static boolean hasConfigFile(File folder) {
        File SyndesisConfigFile = new File(folder, FILE_NAME);
        return SyndesisConfigFile != null && SyndesisConfigFile.exists() && SyndesisConfigFile.isFile();
    }

    /**
     * Creates a configured Jackson object mapper for parsing YAML
     */
    public static ObjectMapper createObjectMapper() {
        YAMLFactory yamlFactory = new YAMLFactory();
        yamlFactory.configure(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID, false);
        return new ObjectMapper(yamlFactory);
    }

    public static SyndesisModel parseSyndesisConfig(File file) throws IOException {
        LOG.info("Loading SyndesisModel flows from file: " + file);
        return parseYaml(file, SyndesisModel.class);
    }

    public static SyndesisModel parseSyndesisConfig(InputStream input) throws IOException {
        return parseYaml(input, SyndesisModel.class);
    }

    public static SyndesisModel parseSyndesisConfig(URL url) throws IOException {
        LOG.info("Loading SyndesisModel flows from URL: " + url);
        return parseYaml(url, SyndesisModel.class);
    }

    public static SyndesisModel parseSyndesisConfig(String yaml) throws IOException {
        return parseYaml(yaml, SyndesisModel.class);
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
     * Saves the syndesis.yml file to the given project directory
     */
    public static boolean saveToFolder(File basedir, SyndesisModel config, boolean overwriteIfExists) throws IOException {
        File file = new File(basedir, SyndesisHelpers.FILE_NAME);
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
    public static boolean saveConfig(SyndesisModel config, File file) throws IOException {
        createObjectMapper().writeValue(file, config);
        return true;
    }

    /**
     * Saves the configuration as JSON in the given file
     */
    public static void saveConfigJSON(SyndesisModel syndesis, File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, syndesis);
    }

}
