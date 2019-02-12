package io.syndesis.server.controller.integration.camelk;

import java.util.Collections;
import java.util.List;

import io.syndesis.server.controller.StateChangeHandler;
import io.syndesis.server.controller.StateChangeHandlerProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "controllers.integration", havingValue = "camel-k")
public class CamelKHandlerProvider implements StateChangeHandlerProvider {

    final private List<StateChangeHandler> handlers;

    protected CamelKHandlerProvider(@Qualifier("camel-k") List <StateChangeHandler> handlers) {
        this.handlers = Collections.unmodifiableList(handlers);
    }

    @Override
    public List<StateChangeHandler> getStatusChangeHandlers() {
        return handlers;
    }
}
