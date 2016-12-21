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
package io.fabric8.funktion.connector.generator;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.funktion.support.Strings;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.utils.DomHelper;
import io.fabric8.utils.IOHelpers;
import io.fabric8.utils.XmlUtils;
import org.apache.camel.catalog.CamelCatalog;
import org.apache.camel.catalog.DefaultCamelCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static io.fabric8.funktion.support.YamlHelper.createYamlMapper;
import static io.fabric8.utils.DomHelper.firstChild;

/**
 */
public class ConnectorGenerator {
    private static final transient Logger LOG = LoggerFactory.getLogger(ConnectorGenerator.class);

    protected static final Set<String> notCamelStarterConnectors = new HashSet<>(Arrays.asList(
            "asterisk",
            "ejb",
            "eventadmin",
            "ibatis",
            "jclouds",
            "mina",
            "paxlogging",
            "quartz",
            "spark-rest"
    ));

    public static void main(String[] args) {
        try {
            ConnectorGenerator generator = new ConnectorGenerator();
            generator.generate();
        } catch (Exception e) {
            System.out.println("Failed: " + e);
            e.printStackTrace();
        }
    }

    public static File getBaseDir() {
        String basedir = System.getProperty("basedir", System.getProperty("user.dir", "."));
        File answer = new File(basedir);
        return answer;
    }

    public void generate() throws IOException, ParserConfigurationException, SAXException, TransformerException {
        CamelCatalog camelCatalog = new DefaultCamelCatalog(true);
        List<String> componentNames = camelCatalog.findComponentNames();
        Collections.sort(componentNames);

        String json = camelCatalog.listComponentsAsJson();
        ObjectMapper mapper = new ObjectMapper();
        List<ComponentModel> components = new ArrayList<>();
        MappingIterator<ComponentModel> iter = mapper.readerFor(ComponentModel.class).readValues(json);
        while (iter.hasNext()) {
            ComponentModel next = iter.next();
            if (next != null) {
                components.add(next);
            }
        }

        File projectsDir = new File(getBaseDir(), "../connectors");
        String projectVersion = System.getProperty("project.version", "1.0-SNAPSHOT");

        File componentsPomFile = new File(projectsDir, "pom.xml");
        Document componentsPom = parseDocument(componentsPomFile);
        boolean updatedComponentsPom = false;
        Element modules = firstChild(componentsPom.getDocumentElement(), "modules");
        if (modules == null) {
            modules = DomHelper.addChildElement(componentsPom.getDocumentElement(), "modules");
        }

        File componentPackagePomFile = new File(getBaseDir(), "../connector-package/pom.xml");
        Document componentPackagePom = parseDocument(componentPackagePomFile);
        Element packageBuild = getOrCreateFirstChild(componentPackagePom.getDocumentElement(), "build");
        Element packagePlugins = getOrCreateFirstChild(packageBuild, "plugins");
        Element packagePlugin = firstChild(packagePlugins, "plugin");
        Element packageDependencies = null;
        if (packagePlugin == null) {
            LOG.error("No <plugin> element inside <build><plugins> for " + componentPackagePomFile);
        } else {
            packageDependencies = firstChild(packagePlugin, "dependencies");
            if (packageDependencies == null) {
                LOG.error("No <dependencies> element inside <build><plugins><plugin> for " + componentPackagePomFile);
            }
        }
        boolean updatedComponentPackagePom = false;


        Set<String> moduleNames = new TreeSet<>();
        int count = 0;
        for (ComponentModel component : components) {
            String componentName = component.getScheme();
            String groupId = component.getGroupId();
            String artifactId = component.getArtifactId();
            String version = component.getVersion();
            String componentTitle = component.getTitle();
            if (Strings.isEmpty(componentTitle)) {
                componentTitle = componentName;
            }

            if (!Strings.isEmpty(componentName) && !Strings.isEmpty(groupId) && !Strings.isEmpty(artifactId) && !Strings.isEmpty(version)) {
                String moduleName = "connector-" + componentName.toLowerCase();
                File projectDir = new File(projectsDir, moduleName);
                projectDir.mkdirs();

                String starterArtifactId = artifactId;
                if (isSpringStarterModule(componentName, artifactId)) {
                    starterArtifactId += "-starter";
                }
                String dependencies = "  \n" +
                        "  <dependencies>\n" +
                        "    <dependency>\n" +
                        "      <groupId>" + groupId + "</groupId>\n" +
                        "      <artifactId>" + starterArtifactId + "</artifactId>\n" +
                        "    </dependency>\n" +
                        "  </dependencies>\n";
                if (artifactId.equals("camel-core")) {
                    dependencies = "";
                }

                moduleNames.add(moduleName);
                if (addModuleNameIfMissing(modules, moduleName)) {
                    updatedComponentsPom = true;
                }
                if (addPackageDependency(packageDependencies, moduleName)) {
                    updatedComponentPackagePom = true;
                }

                String pomXml = "<!--\n" +
                        "  ~ Copyright 2016 Red Hat, Inc.\n" +
                        "  ~ <p>\n" +
                        "  ~ Red Hat licenses this file to you under the Apache License, version\n" +
                        "  ~ 2.0 (the \"License\"); you may not use this file except in compliance\n" +
                        "  ~ with the License.  You may obtain a copy of the License at\n" +
                        "  ~ <p>\n" +
                        "  ~ http://www.apache.org/licenses/LICENSE-2.0\n" +
                        "  ~ <p>\n" +
                        "  ~ Unless required by applicable law or agreed to in writing, software\n" +
                        "  ~ distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                        "  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or\n" +
                        "  ~ implied.  See the License for the specific language governing\n" +
                        "  ~ permissions and limitations under the License.\n" +
                        "  ~\n" +
                        "  -->\n" +
                        "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                        "\n" +
                        "  <modelVersion>4.0.0</modelVersion>\n" +
                        "\n" +
                        "  <parent>\n" +
                        "    <groupId>io.fabric8.funktion.connector</groupId>\n" +
                        "    <artifactId>connectors</artifactId>\n" +
                        "    <version>" + projectVersion + "</version>\n" +
                        "  </parent>\n" +
                        "\n" +
                        "  <groupId>io.fabric8.funktion.connector</groupId>\n" +
                        "  <artifactId>" + moduleName + "</artifactId>\n" +
                        "  <version>1.0-SNAPSHOT</version>\n" +
                        "  <packaging>jar</packaging>\n" +
                        "\n" +
                        "  <name>Funktion Connector " + componentTitle + "</name>\n" +
                        "  <description>Funktion :: Connector :: " + componentTitle + "</description>\n" +
                        dependencies +
                        "\n" +
                        "  <build>\n" +
                        "    <plugins>\n" +
                        "      <plugin>\n" +
                        "        <groupId>io.fabric8</groupId>\n" +
                        "        <artifactId>fabric8-maven-plugin</artifactId>\n" +
                        "        <version>${fabric8.maven.plugin.version}</version>\n" +
                        "        <executions>\n" +
                        "          <execution>\n" +
                        "            <goals>\n" +
                        "              <goal>resource</goal>\n" +
                        "              <goal>build</goal>\n" +
                        "            </goals>\n" +
                        "          </execution>\n" +
                        "        </executions>\n" +
                        "        <configuration>\n" +
                        "          <generator>\n" +
                        "            <config>\n" +
                        "              <spring-boot>\n" +
                        "                <name>fabric8/%a:%v</name>\n" +
                        "                <alias>funktor</alias>\n" +
                        "              </spring-boot>\n" +
                        "            </config>\n" +
                        "          </generator>\n" +
                        "          <enricher>\n" +
                        "            <excludes>\n" +
                        "              <exclude>fmp-controller</exclude>\n" +
                        "              <exclude>fmp-service</exclude>\n" +
                        "            </excludes>\n" +
                        "          </enricher>\n" +
                        "        </configuration>\n" +
                        "      </plugin>\n" +
                        "      <plugin>\n" +
                        "        <groupId>org.springframework.boot</groupId>\n" +
                        "        <artifactId>spring-boot-maven-plugin</artifactId>\n" +
                        "      </plugin>\n" +
                        "    </plugins>\n" +
                        "  </build>\n" +
                        "</project>";
                IOHelpers.writeFully(new File(projectDir, "pom.xml"), pomXml);

                String dummyJavaClass = "package io.fabric8.funktion.connector;\n" +
                        "\n" +
                        "public class ConnectorMarker{}\n";
    
                File dummyJavaFile = new File(projectDir, "src/main/java/io/fabric8/funktion/connector/ConnectorMarker.java");
                dummyJavaFile.getParentFile().mkdirs();
                IOHelpers.writeFully(dummyJavaFile, dummyJavaClass);


                String jSonSchema = camelCatalog.componentJSonSchema(componentName);
                String asciiDoc = camelCatalog.componentAsciiDoc(componentName);

                String image = "fabric8/" + moduleName + ":${project.version}";
                File applicationPropertiesFile = new File(projectDir, "src/main/funktion/application.properties");
                ConfigMap configMap = Connectors.createConnector(component, jSonSchema, asciiDoc, image, applicationPropertiesFile);
                File configMapFile = new File(projectDir, "src/main/fabric8/" + componentName + "-cm.yml");
                configMapFile.getParentFile().mkdirs();

                ObjectMapper yamlMapper = createYamlMapper();
                yamlMapper.writer().writeValue(configMapFile, configMap);
                count++;
            }
        }
        if (updatedComponentsPom) {
            updateDocument(componentsPomFile, componentsPom);
        }
        if (updatedComponentPackagePom) {
            updateDocument(componentPackagePomFile, componentPackagePom);
        }

        String moduleNamesText = io.fabric8.utils.Strings.join(moduleNames, "', '");
        String releaseImagesGroovy = "#!/usr/bin/groovy\n" +
                "def imagesBuiltByPipeline() {\n" +
                "  return ['connector-amq', '" + moduleNamesText + "']\n" +
                "}\n" +
                "return this;\n";
        IOHelpers.writeFully(new File(getBaseDir(), "../releaseImages.groovy"), releaseImagesGroovy);


        LOG.info("Generated " + count + " connectors");
    }

    protected boolean isSpringStarterModule(String connectorName, String artifactId) {
        // TODO from 2.19 of camel use the catalog to know this
        // for now lets hard code the answers
        return !notCamelStarterConnectors.contains(connectorName);
    }

    protected void updateDocument(File file, Document doc) throws FileNotFoundException, TransformerException {
        LOG.info("Updating the pom " + file);
        try {
            DomHelper.save(doc, file);
        } catch (Exception e) {
            LOG.error("Failed to update pom " + file + ". " + e, e);
            throw e;
        }
    }

    protected Document parseDocument(File pom) throws ParserConfigurationException, SAXException, IOException {
        Document doc;
        try {
            doc = XmlUtils.parseDoc(pom);
        } catch (Exception e) {
            LOG.error("Failed to parse pom " + pom + ". " + e, e);
            throw e;
        }
        return doc;
    }

    protected Element getOrCreateFirstChild(Element element, String elementName) {
        Element answer = DomHelper.firstChild(element, elementName);
        if (answer != null) {
            return answer;
        }
        return DomHelper.addChildElement(element, elementName);
    }

    protected boolean addModuleNameIfMissing(Element modules, String moduleName) {
        NodeList childNodes = modules.getChildNodes();
        if (childNodes != null) {
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node item = childNodes.item(i);
                if (item instanceof Element) {
                    Element property = (Element) item;
                    if (moduleName.equals(property.getTextContent())) {
                        return false;
                    }
                }
            }
        }
        modules.appendChild(modules.getOwnerDocument().createTextNode("\n      "));
        DomHelper.addChildElement(modules, "module", moduleName);
        return true;
    }

    protected boolean addPackageDependency(Element dependencies, String moduleName) {
        NodeList childNodes = dependencies.getChildNodes();
        if (childNodes != null) {
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node item = childNodes.item(i);
                if (item instanceof Element) {
                    Element dependency = (Element) item;
                    Element artifactId = DomHelper.firstChild(dependency, "artifactId");
                    if (artifactId != null && moduleName.equals(artifactId.getTextContent())) {
                        return false;
                    }
                }
            }
        }
        dependencies.appendChild(dependencies.getOwnerDocument().createTextNode("\n          "));
        Element dependency = DomHelper.addChildElement(dependencies, "dependency");
        dependency.appendChild(dependencies.getOwnerDocument().createTextNode("\n            "));
        DomHelper.addChildElement(dependency, "groupId", "io.fabric8.funktion.connector");
        dependency.appendChild(dependencies.getOwnerDocument().createTextNode("\n            "));
        DomHelper.addChildElement(dependency, "artifactId", moduleName);
        dependency.appendChild(dependencies.getOwnerDocument().createTextNode("\n            "));
        DomHelper.addChildElement(dependency, "version", "${project.version}");
        dependency.appendChild(dependencies.getOwnerDocument().createTextNode("\n          "));
        return true;
    }
}
