package io.syndesis.server.api.generator.swagger;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.action.Action;
import io.syndesis.common.model.action.ActionsSummary;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.action.ConnectorDescriptor;
import io.syndesis.common.model.api.APISummary;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.integration.StepKind;
import io.syndesis.common.util.KeyGenerator;
import io.syndesis.common.util.SyndesisServerException;
import io.syndesis.server.api.generator.APIGenerator;
import io.syndesis.server.api.generator.APIValidationContext;
import io.syndesis.server.api.generator.ProvidedApiTemplate;
import io.syndesis.server.api.generator.swagger.util.SwaggerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

public class SwaggerAPIGenerator implements APIGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(SwaggerAPIGenerator.class);

    private DataShapeGenerator dataShapeGenerator;

    public SwaggerAPIGenerator() {
        this.dataShapeGenerator = new UnifiedDataShapeGenerator();
    }

    @Override
    public APISummary info(String specification, APIValidationContext validation) {
        final SwaggerModelInfo swaggerInfo = SwaggerHelper.parse(specification, validation);
        try {
            // No matter if the validation fails, try to process the swagger
            final Map<String, Path> paths = swaggerInfo.getModel().getPaths();

            final AtomicInteger total = new AtomicInteger(0);

            final Map<String, Integer> tagCounts = paths.entrySet().stream()//
                .flatMap(p -> p.getValue().getOperations().stream())//
                .peek(o -> total.incrementAndGet())//
                .flatMap(o -> ofNullable(o.getTags()).orElse(Collections.emptyList()).stream().distinct())//
                .collect(//
                    Collectors.groupingBy(//
                        Function.identity(), //
                        Collectors.reducing(0, (e) -> 1, Integer::sum)//
                    ));

            final ActionsSummary actionsSummary = new ActionsSummary.Builder()//
                .totalActions(total.intValue())//
                .actionCountByTags(tagCounts)//
                .build();

            return new APISummary.Builder()//
                .name(swaggerInfo.getModel().getInfo().getTitle())//
                .description(swaggerInfo.getModel().getInfo().getDescription())//
                .actionsSummary(actionsSummary)//
                .errors(swaggerInfo.getErrors())//
                .warnings(swaggerInfo.getWarnings())//
                .putConfiguredProperty("specification", swaggerInfo.getResolvedSpecification())//
                .build();
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") final Exception ex) {
            if (!swaggerInfo.getErrors().isEmpty()) {
                // Just log and return the validation errors if any
                LOG.error("An error occurred while trying to validate a API", ex);
                return new APISummary.Builder().errors(swaggerInfo.getErrors()).warnings(swaggerInfo.getWarnings()).build();
            }

            throw SyndesisServerException.launderThrowable("An error occurred while trying to validate a API", ex);
        }
    }

    @Override
    public Integration generateIntegration(String specification, ProvidedApiTemplate template) {
        SwaggerModelInfo info = SwaggerHelper.parse(specification, APIValidationContext.NONE);
        Swagger swagger = info.getModel();

        String name = Optional.ofNullable(swagger.getInfo())
            .flatMap(i -> Optional.ofNullable(i.getTitle()))
            .orElse(null);

        Integration.Builder integration = new Integration.Builder()
            .id(KeyGenerator.createKey())
            .name(name);

        Map<String, Path> paths = swagger.getPaths();
        for (Map.Entry<String, Path> pathEntry : paths.entrySet()) {
            Path path = pathEntry.getValue();

            for (Map.Entry<HttpMethod, Operation> operationEntry : path.getOperationMap().entrySet()) {
                Operation operation = operationEntry.getValue();

                String operationName = operation.getSummary();
                String operationDescription = operationEntry.getKey() + " " + pathEntry.getKey();

                String operationId = operation.getOperationId();
                if (operationId == null) {
                    // TODO relax this constraint using strategy from connector generator
                    throw new IllegalArgumentException("Missing operation ID operation " + operationName + " at " + operationDescription);
                }



                String key = KeyGenerator.createKey();

                DataShape startDataShape = dataShapeGenerator.createShapeFromRequest(info.getResolvedJsonGraph(), swagger, operation);
                Action startAction = template.getStartAction().orElseThrow(() -> new IllegalStateException("cannot find start action"));
                Action modifiedStartAction = new ConnectorAction.Builder()
                    .createFrom(startAction)
                    .descriptor(new ConnectorDescriptor.Builder()
                        .createFrom(startAction.getDescriptor())
                        .outputDataShape(startDataShape)
                        .build())
                    .build();
                Step startStep = new Step.Builder()
                    .id(KeyGenerator.createKey())
                    .action(modifiedStartAction)
                    .connection(template.getConnection())
                    .stepKind(StepKind.endpoint)
                    .putConfiguredProperty("name", operationId)
                    .putMetadata("configured", "true")
                    .build();

                DataShape endDataShape = dataShapeGenerator.createShapeFromResponse(info.getResolvedJsonGraph(), swagger, operation);
                Action endAction = template.getEndAction().orElseThrow(() -> new IllegalStateException("cannot find end action"));
                Action modifiedEndAction = new ConnectorAction.Builder()
                    .createFrom(endAction)
                    .descriptor(new ConnectorDescriptor.Builder()
                        .createFrom(endAction.getDescriptor())
                        .inputDataShape(endDataShape)
                        .build())
                    .build();
                Step endStep = new Step.Builder()
                    .id(KeyGenerator.createKey())
                    .action(modifiedEndAction)
                    .connection(template.getConnection())
                    .stepKind(StepKind.endpoint)
                    .putConfiguredProperty("name", "501") // TODO set the 501 return code on the right property
                    .putMetadata("configured", "true")
                    .build();

                Flow flow = new Flow.Builder()
                    .id(key)
                    .addStep(startStep)
                    .addStep(endStep)
                    .name(operationName)
                    .description(operationDescription)
                    .build();

                integration = integration.addFlow(flow);
            }

        }

        return integration.build();
    }
}
