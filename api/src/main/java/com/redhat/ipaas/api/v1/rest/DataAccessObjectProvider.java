package com.redhat.ipaas.api.v1.rest;

import java.util.List;

public interface DataAccessObjectProvider {

    List<DataAccessObject> getDataAccessObjects();
}
