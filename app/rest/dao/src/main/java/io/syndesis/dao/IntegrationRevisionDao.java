package io.syndesis.dao;

import io.syndesis.dao.manager.DataAccessObject;
import io.syndesis.model.integration.IntegrationRevision;

public interface IntegrationRevisionDao extends DataAccessObject<IntegrationRevision> {
    @Override
    default Class<IntegrationRevision> getType() {
        return IntegrationRevision.class;
    }
}
