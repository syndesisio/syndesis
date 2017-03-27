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
package com.redhat.ipaas.verifier;

import com.redhat.ipaas.model.connection.Connector;
import com.redhat.ipaas.project.converter.ProjectGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

@Component
@ConditionalOnProperty(value = "verifier.kind", havingValue = "forking")
public class ForkingVerifier implements Verifier {

    private final ProjectGenerator projectGenerator;
    private String localMavenRepoLocation = null; // "/tmp/ipaas-local-mvn-repo";

    public ForkingVerifier(ProjectGenerator projectGenerator) {
        this.projectGenerator = projectGenerator;
    }

    @Override
    public Result verify(Connector connector, Scope scope, Map<String, String> props) {
        Properties configuredProperties = new Properties();
        for (Map.Entry<String, String> entry : props.entrySet()) {
            configuredProperties.put(entry.getKey(), entry.getValue());
        }

        if (!connector.getCamelConnectorGAV().isPresent() ||
            !connector.getCamelConnectorPrefix().isPresent() ) {
            return createResult(scope, Result.Status.UNSUPPORTED, null);
        }

        try {
            // we could cache the connectorClasspath.
            String connectorClasspath = getConnectorClasspath(connector);

            // shell out to java to validate the properties.
            Properties result = runValidator(connectorClasspath, scope, connector.getCamelConnectorPrefix().get(), configuredProperties);
            String value = result.getProperty("value");
            if ("error".equals(value)) {
                return createResult(scope, Result.Status.ERROR, result);
            }
            if ( "unsupported".equals(value) ) {
                return createResult(scope, Result.Status.UNSUPPORTED, result);
            }
            if ( "ok".equals(value) ) {
                return createResult(scope, Result.Status.OK, result);
            }
            return createResult(scope, Result.Status.ERROR, result);
        } catch (IOException|InterruptedException e) {
            return createResult(scope, Result.Status.ERROR, null);
        }
    }

    private ImmutableResult createResult(Scope scope, Result.Status status, Properties response) {
        ImmutableResult.Builder builder = ImmutableResult.builder().scope(scope).status(status);
        if( response != null ) {
            LinkedHashMap<String, Error> errors = new LinkedHashMap<>();
            for (Map.Entry<Object, Object> entry : response.entrySet()) {
                String key = (String) entry.getKey();
                if( key.startsWith("error.") ) {
                    String errorId = key.substring("error.".length()).replaceFirst("\\..*", "");
                    Error error = errors.getOrDefault(errorId, createError());
                    String value = (String) entry.getValue();
                    if( key.endsWith(".code") ) {
                        error = error.withCode(value);
                    }
                    if( key.endsWith(".description") ) {
                        error = error.withDescription(value);
                    }
                    errors.put(errorId, error);
                }
            }
            builder.addAllErrors(errors.values());
        }
        return builder.build();
    }

    private Error createError() {
        return ImmutableError.builder().build();
    }

    private Properties runValidator(String classpath, Scope scope, String camelPrefix, Properties request) throws IOException, InterruptedException {
        Process java = new ProcessBuilder()
            .command(
                "java", "-classpath", classpath,
                "com.redhat.ipaas.connector.ConnectorVerifier", scope.toString(), camelPrefix
            )
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start();

        try (OutputStream os = java.getOutputStream()) {
            request.store(os, null);
        }
        Properties result = new Properties();
        try (InputStream is = java.getInputStream()) {
            result.load(is);
        }

        if (java.waitFor() != 0) {
            throw new IOException("Verifier failed with exit code: " + java.exitValue());
        }
        return result;
    }


    private String getConnectorClasspath(Connector connector) throws IOException, InterruptedException {
        byte[] pom = projectGenerator.generatePom(connector);
        java.nio.file.Path tmpDir = Files.createTempDirectory("ipaas-connector");
        try {
            Files.write(tmpDir.resolve("pom.xml"), pom);
            ArrayList<String> args = new ArrayList<>();
            args.add("mvn");
            args.add("org.apache.maven.plugins:maven-dependency-plugin:3.0.0:build-classpath");
            if (localMavenRepoLocation != null) {
                args.add("-Dmaven.repo.local=" + localMavenRepoLocation);
            }
            ProcessBuilder builder = new ProcessBuilder().command(args)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .directory(tmpDir.toFile());
            Map<String, String> environment = builder.environment();
            environment.put("MAVEN_OPTS", "-Xmx64M");
            Process mvn = builder.start();
            try {
                String result = parseClasspath(mvn.getInputStream());
                if (mvn.waitFor() != 0) {
                    throw new IOException("Could not get the connector classpath, mvn exit value: " + mvn.exitValue());
                }
                return result;
            } finally {
                mvn.getInputStream().close();
                mvn.getOutputStream().close();
            }
        } finally {
            FileSystemUtils.deleteRecursively(tmpDir.toFile());
        }
    }

    private String parseClasspath(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        boolean useNextLine = true;
        String result = null;
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println("mvn: "+line);
            if (useNextLine) {
                useNextLine = false;
                result = line;
            }
            if (line.startsWith("[INFO] Dependencies classpath:")) {
                useNextLine = true;
            }
        }
        return result;
    }
}
