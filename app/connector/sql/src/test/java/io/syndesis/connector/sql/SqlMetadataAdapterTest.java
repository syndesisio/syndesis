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
package io.syndesis.connector.sql;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.connector.sql.common.SqlTest;
import io.syndesis.connector.sql.common.SqlTest.ConnectionInfo;
import io.syndesis.connector.sql.common.SqlTest.Setup;
import io.syndesis.connector.sql.common.SqlTest.Teardown;
import io.syndesis.connector.sql.stored.SqlStoredConnectorMetaDataExtension;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;

import org.apache.camel.component.extension.MetaDataExtension.MetaData;
import org.apache.camel.component.extension.metadata.AbstractMetaDataExtension;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.fasterxml.jackson.databind.ObjectWriter;

import static java.util.Collections.emptyMap;

import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

@ExtendWith(SqlTest.class)
@Setup("CREATE TABLE NAME (ID INTEGER PRIMARY KEY, FIRSTNAME VARCHAR(255), LASTNAME VARCHAR(255))")
@Teardown("DROP TABLE NAME")
public class SqlMetadataAdapterTest {

    private static final DefaultCamelContext CAMEL_CONTEXT = new DefaultCamelContext();

    private static final SqlConnectorMetaDataExtension SQL_CONNECTOR_META = new SqlConnectorMetaDataExtension(CAMEL_CONTEXT);

    private static final SqlStoredConnectorMetaDataExtension SQL_STORED_CONNECTOR_META = new SqlStoredConnectorMetaDataExtension(CAMEL_CONTEXT);

    private static final ObjectWriter WRITTER = JsonUtils.writer();

    private final ConnectionInfo info;

    public SqlMetadataAdapterTest(final ConnectionInfo info) {
        this.info = info;
    }

    @ParameterizedTest(name = "sql-connector: {0}")
    @CsvSource(
        delimiterString = "->",
        value = {
            "query=SELECT * FROM NAME -> name_sql_no_param_metadata.json",
            "query=SELECT * FROM NAME WHERE ID=:#id -> name_sql_metadata.json",
            "query=INSERT INTO NAME (FIRSTNAME, LASTNAME) VALUES ('Sheldon', 'Cooper') -> name_sql_update_no_param_metadata.json",
            "query=INSERT INTO NAME (FIRSTNAME, LASTNAME) VALUES (:#firstname, :#lastname) -> name_sql_update_metadata.json",
            "query=INSERT INTO NAME (FIRSTNAME, LASTNAME) VALUES (:#firstname, :#lastname); batch=true -> name_sql_batch_update_metadata.json"
        })
    public void sqlConnectorCases(final String parameters, final String jsonPath)
        throws IOException, JSONException {
        final SyndesisMetadata metadata = fetchMetadata(info, SQL_CONNECTOR_META, "sql-connector", mapFrom(parameters));
        assertMetadataEqualTo(metadata, jsonPath);
    }

    @ParameterizedTest(name = "sql-stored-connector: {0}")
    @CsvSource(
        delimiterString = "->",
        value = {
            " -> stored_procedure_list.json",
            "Pattern=From -> stored_start_procedure_list.json",
            "procedureName=DEMO_ADD -> demo_add_metadata.json"
        })
    @Setup({
        "CREATE PROCEDURE DEMO_ADD2(IN A INTEGER, IN B INTEGER, OUT C INTEGER) PARAMETER STYLE JAVA LANGUAGE JAVA  EXTERNAL NAME 'io.syndesis.connector.SampleStoredProcedures.demo_add'",
        "CREATE PROCEDURE DEMO_ADD(IN A INTEGER, IN B INTEGER, OUT C INTEGER) PARAMETER STYLE JAVA LANGUAGE JAVA EXTERNAL NAME 'io.syndesis.connector.SampleStoredProcedures.demo_add'",
        "CREATE PROCEDURE DEMO_OUT(OUT C INTEGER) PARAMETER STYLE JAVA LANGUAGE JAVA EXTERNAL NAME 'io.syndesis.connector.SampleStoredProcedures.demo_add'"
    })
    @Teardown({
        "DROP PROCEDURE DEMO_ADD2",
        "DROP PROCEDURE DEMO_ADD",
        "DROP PROCEDURE DEMO_OUT",
    })
    public void sqlStoredCases(final String parameters, final String jsonPath) throws IOException, JSONException {
        final SyndesisMetadata metadata = fetchMetadata(info, SQL_STORED_CONNECTOR_META, "sql-stored-connector", mapFrom(parameters));
        assertMetadataEqualTo(metadata, jsonPath);
    }

    @AfterAll
    public static void shutdown() throws Exception {
        CAMEL_CONTEXT.stop();
    }

    static void assertMetadataEqualTo(final SyndesisMetadata metadata, final String fileName) throws IOException, JSONException {
        try (InputStream resource = SqlMetadataAdapterTest.class.getResourceAsStream("/sql/" + fileName)) {
            final String expectedMetadata = IOUtils.toString(resource, StandardCharsets.UTF_8).trim();

            final String actualMetadata = WRITTER.writeValueAsString(metadata);

            assertEquals(expectedMetadata, actualMetadata, JSONCompareMode.STRICT);
        }
    }

    static SyndesisMetadata fetchMetadata(final ConnectionInfo info, final AbstractMetaDataExtension metadataExtension, final String connectorId,
        final Map<String, String> additionalParameters) {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("user", info.username);
        parameters.put("password", info.password);
        parameters.put("url", info.url);
        parameters.putAll(additionalParameters);

        final Optional<MetaData> metadata = metadataExtension.meta(parameters);
        final SqlMetadataRetrieval adapter = new SqlMetadataRetrieval();

        return adapter.adapt(metadataExtension.getCamelContext(), "sql", connectorId, parameters, metadata.get());
    }

    static Map<String, String> mapFrom(final String additionalParameters) {
        if (additionalParameters == null) {
            return emptyMap();
        }

        return Stream.of(additionalParameters.split(";\\s+"))
            .map(s -> s.split("=", 2))
            .collect(Collectors.toMap(a -> a[0], a -> a[1]));
    }
}
