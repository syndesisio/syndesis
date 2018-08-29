package io.syndesis.server.api.generator.swagger;

import io.swagger.models.Path;
import io.syndesis.common.model.action.ActionsSummary;
import io.syndesis.common.model.api.APISummary;
import io.syndesis.common.util.SyndesisServerException;
import io.syndesis.server.api.generator.APIInspector;
import io.syndesis.server.api.generator.APIValidationContext;
import io.syndesis.server.api.generator.swagger.util.SwaggerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

public class SwaggerAPIInspector implements APIInspector {

    private static final Logger LOG = LoggerFactory.getLogger(SwaggerAPIInspector.class);

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
                LOG.error("An error occurred while trying to create an OpenAPI connector", ex);
                return new APISummary.Builder().errors(swaggerInfo.getErrors()).warnings(swaggerInfo.getWarnings()).build();
            }

            throw SyndesisServerException.launderThrowable("An error occurred while trying to create an OpenAPI connector", ex);
        }
    }

}
