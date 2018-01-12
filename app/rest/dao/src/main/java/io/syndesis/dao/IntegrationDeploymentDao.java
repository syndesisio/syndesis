package io.syndesis.dao;

import io.syndesis.dao.manager.DataAccessObject;
import io.syndesis.model.integration.IntegrationDeployment;

public interface IntegrationDeploymentDao extends DataAccessObject<IntegrationDeployment> {
    @Override
    default Class<IntegrationDeployment> getType() {
        return IntegrationDeployment.class;
    }
}
