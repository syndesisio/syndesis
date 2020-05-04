/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.dv.server.endpoint;

import java.util.ArrayList;
import java.util.List;

import io.syndesis.dv.model.TablePrivileges;

public class RoleInfo {

    public enum Operation {
        GRANT, REVOKE
    }

    private List<TablePrivileges> tablePrivileges = new ArrayList<TablePrivileges>();
    private Operation operation = Operation.GRANT;

    public List<TablePrivileges> getTablePrivileges() {
        return tablePrivileges;
    }

    public void setTablePrivileges(List<TablePrivileges> tablePrivileges) {
        this.tablePrivileges = tablePrivileges;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

}
