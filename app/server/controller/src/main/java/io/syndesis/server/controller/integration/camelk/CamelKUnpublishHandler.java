package io.syndesis.server.controller.integration.camelk;

import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;

import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.IntegrationDeploymentState;
import io.syndesis.server.controller.StateChangeHandler;
import io.syndesis.server.controller.StateUpdate;
import io.syndesis.server.controller.integration.IntegrationPublishValidator;
import io.syndesis.server.controller.integration.BaseHandler;
import io.syndesis.server.dao.IntegrationDao;
import io.syndesis.server.dao.IntegrationDeploymentDao;
import io.syndesis.server.openshift.OpenShiftService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@Qualifier("camel-k")
@ConditionalOnProperty(value = "controllers.integration", havingValue = "camel-k")
public class CamelKUnpublishHandler extends BaseHandler implements StateChangeHandler {

    protected CamelKUnpublishHandler(OpenShiftService osService,
                                     IntegrationDao iDao,
                                     IntegrationDeploymentDao idDao,
                                     IntegrationPublishValidator validator) {
        super(osService, iDao, idDao, validator);
    }

    @Override
    public Set<IntegrationDeploymentState> getTriggerStates() {
        return Collections.singleton(IntegrationDeploymentState.Unpublished);
    }

    @Override
    public StateUpdate execute(IntegrationDeployment integrationDeployment) {
        return null;
    }

    @Override
    public void execute(IntegrationDeployment integrationDeployment, Consumer<StateUpdate> updates) {

    }
}
