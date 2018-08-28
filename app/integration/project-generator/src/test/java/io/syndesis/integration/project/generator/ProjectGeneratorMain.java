package io.syndesis.integration.project.generator;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import io.syndesis.common.model.Dependency;
import io.syndesis.common.model.Kind;
import io.syndesis.common.model.ResourceIdentifier;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.model.openapi.OpenApi;
import io.syndesis.common.util.KeyGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectGeneratorMain {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectGeneratorMain.class);

    public static void main(String[] args) throws Exception {
        TestResourceManager resourceManager = new TestResourceManager();

        // ******************
        // OpenApi
        // ******************

        URL location = ProjectGeneratorTest.class.getResource("/petstore.json");
        byte[] content = Files.readAllBytes(Paths.get(location.toURI()));

        resourceManager.put("petstore", new OpenApi.Builder().document(content).id("petstore").build());

        // ******************
        // Integration
        // ******************

        Step s1 = new Step.Builder()
            .stepKind(StepKind.endpoint)
            .action(new ConnectorAction.Builder()
                .id(KeyGenerator.createKey())
                .descriptor(new ConnectorDescriptor.Builder()
                    .connectorId("new")
                    .componentScheme("direct")
                    .putConfiguredProperty("name", "getPetById")
                    .build())
                .build())
            .connection(new Connection.Builder()
                .connector(
                    new Connector.Builder()
                        .id("api-provider")
                        .addDependency(new Dependency.Builder()
                            .type(Dependency.Type.MAVEN)
                            .id("io.syndesis.connector:connector-api-provider:1.x.x")
                            .build())
                        .build())
                .build())
            .build();
        Step s2 = new Step.Builder()
            .stepKind(StepKind.endpoint)
            .action(new ConnectorAction.Builder()
                .id(KeyGenerator.createKey())
                .descriptor(new ConnectorDescriptor.Builder()
                    .connectorId("new")
                    .componentScheme("log")
                    .putConfiguredProperty("loggerName", "getPetById")
                    .putConfiguredProperty("showAll", "true")
                    .putConfiguredProperty("multiline", "true")
                    .build())
                .build())
            .build();

        Integration integration = new Integration.Builder()
            .id("test-integration")
            .name("Test Integration")
            .description("This is a test integration!")
            .addResource(new ResourceIdentifier.Builder()
                .kind(Kind.OpenApi)
                .id("petstore")
                .build())
            .addFlow(new Flow.Builder()
                .steps(Arrays.asList(s1, s2))
                .build())
            .build();

        ProjectGeneratorConfiguration configuration = new ProjectGeneratorConfiguration();
        Path destination = Files.createTempDirectory("integration-project");
        ProjectGeneratorTest.generate(destination, integration, configuration, resourceManager);

        LOGGER.info("Project written to: {}", destination);
    }

}
