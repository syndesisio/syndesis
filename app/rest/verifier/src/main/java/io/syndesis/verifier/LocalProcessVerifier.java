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
package io.syndesis.verifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import io.syndesis.model.connection.Connector;
import io.syndesis.project.converter.ProjectGenerator;

import org.springframework.util.FileSystemUtils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

// Needs to be adapted to new API signature.
/*
 * Sorry for messing this up to adapt to the new API which is used by the external verifier.
 * Maybe its worth to have a look at the ExternalVerifier and whether we should not fully settle on
 * this better decoupled, less resource hungry and more robust approach.
 */

//@Component
//@ConditionalOnProperty(value = "verifier.kind", havingValue = "forking")
@SuppressWarnings("PMD") // Perhaps we should just delete this class and put it out of it's missery
public class LocalProcessVerifier {

    private final ProjectGenerator projectGenerator;

    @SuppressFBWarnings("UWF_NULL_FIELD")
    private String localMavenRepoLocation = null; // "/tmp/syndesis-local-mvn-repo";

    public LocalProcessVerifier(ProjectGenerator projectGenerator) {
        this.projectGenerator = projectGenerator;
    }

    // Return a list of verification results. Omit scope, use only a connector id for the verification
    public Verifier.Result verify(Connector connector, Verifier.Scope scope, Map<String, String> props) {
        Properties configuredProperties = new Properties();
        for (Map.Entry<String, String> entry : props.entrySet()) {
            configuredProperties.put(entry.getKey(), entry.getValue());
        }

        /*
        // The connector must get the GAV from one of its associated Actions. There should be a 'default'
        // or 'verifier' actions which is selected for doing the verification. The prefix should also come from
        // the action, too.

        if (!connector.getCamelConnectorGAV().isPresent() ||
            !connector.getCamelConnectorPrefix().isPresent() ) {
            return createResult(scope, Verifier.Result.Status.UNSUPPORTED, null);
        }
        */

        try {
            // we could cache the connectorClasspath.
            String connectorClasspath = getConnectorClasspath(connector);

            // shell out to java to validate the properties.
            Properties result = runValidator(connectorClasspath, scope, /* see commment above connector.getCamelConnectorPrefix().get() */ null, configuredProperties);
            String value = result.getProperty("value");
            if ("error".equals(value)) {
                return createResult(scope, Verifier.Result.Status.ERROR, result);
            }
            if ( "unsupported".equals(value) ) {
                return createResult(scope, Verifier.Result.Status.UNSUPPORTED, result);
            }
            if ( "ok".equals(value) ) {
                return createResult(scope, Verifier.Result.Status.OK, result);
            }
            return createResult(scope, Verifier.Result.Status.ERROR, result);
        } catch (IOException|InterruptedException e) {
            return createResult(scope, Verifier.Result.Status.ERROR, null);
        }
    }

    private ImmutableResult createResult(Verifier.Scope scope, Verifier.Result.Status status, Properties response) {
        ImmutableResult.Builder builder = ImmutableResult.builder().scope(scope).status(status);
        if( response != null ) {
            LinkedHashMap<String, ImmutableVerifierError.Builder> errors = new LinkedHashMap<>();
            for (Map.Entry<Object, Object> entry : response.entrySet()) {
                String key = (String) entry.getKey();
                if( key.startsWith("error.") ) {
                    String errorId = key.substring("error.".length()).replaceFirst("\\..*", "");
                    ImmutableVerifierError.Builder error = errors.getOrDefault(errorId, ImmutableVerifierError.builder());
                    String value = (String) entry.getValue();
                    if( key.endsWith(".code") ) {
                        error.code(value);
                    }
                    if( key.endsWith(".description") ) {
                        error.description(value);
                    }
                    errors.put(errorId, error);
                }
            }
            builder.addAllErrors(errors.values().stream().map(ImmutableVerifierError.Builder::build).collect(Collectors.toList()));
        }
        return builder.build();
    }

    private Properties runValidator(String classpath, Verifier.Scope scope, String camelPrefix, Properties request) throws IOException, InterruptedException {
        Process java = new ProcessBuilder()
            .command(
                "java", "-classpath", classpath,
                "io.syndesis.connector.ConnectorVerifier", scope.toString(), camelPrefix
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
        byte[] pom = new byte[0]; // TODO: Fix generation to use an Action projectGenerator.generatePom(connector);
        java.nio.file.Path tmpDir = Files.createTempDirectory("syndesis-connector");
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
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
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
