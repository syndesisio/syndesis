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

package io.syndesis.dv.model.export;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.syndesis.dv.model.TablePrivileges;
import io.syndesis.dv.model.TablePrivileges.Privilege;
import io.syndesis.dv.model.export.v1.TablePrivilegeV1Adapter;
import io.syndesis.dv.rest.JsonMarshaller;

@SuppressWarnings("nls")
public class TablePrivilegeExportTest {

    @Test public void testRoundTrip() {
        TablePrivileges privileges = new TablePrivileges();
        privileges.setRoleName("role");
        privileges.setViewDefinitionId("id");
        privileges.addPrivilege(Privilege.S).addPrivilege(Privilege.D);

        TablePrivilegeV1Adapter adapter = new TablePrivilegeV1Adapter(privileges);

        String expected = "{\n" +
                "  \"grantPrivileges\" : [ \"SELECT\", \"DELETE\" ],\n" +
                "  \"role\" : \"role\",\n" +
                "  \"viewDefinitionId\" : \"id\"\n" +
                "}";
        assertEquals(expected, JsonMarshaller.marshall(adapter));

        TablePrivilegeV1Adapter privileges2 = JsonMarshaller.unmarshall(expected, TablePrivilegeV1Adapter.class);
        assertEquals(privileges.getGrantPrivileges(), privileges2.getGrantPrivileges());
    }

}
