package com.redhat.ipaas.api.v1.rest;

import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class DefaultDataAccessObjectProvider implements DataAccessObjectProvider {

    @Inject
    private IntegrationDAO integrationDAO;

    @Inject
    private IntegrationPatternDAO integrationPatternDAO;

    public List<DataAccessObject> getDataAccessObjects() {
        return Arrays.asList(integrationDAO, integrationPatternDAO);
    }
}
