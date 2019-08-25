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
package io.syndesis.connector.sql.customizer;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import javax.sql.DataSource;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.SqlParameterValue;

import io.syndesis.common.util.Json;
import io.syndesis.common.util.SyndesisConnectorException;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.connector.sql.common.DbMetaDataHelper;

import io.syndesis.connector.sql.common.CamelSqlConstants;
import io.syndesis.connector.sql.common.JSONBeanUtil;
import io.syndesis.connector.sql.common.SqlParam;
import io.syndesis.connector.sql.common.SqlStatementMetaData;
import io.syndesis.connector.sql.common.SqlStatementParser;
import io.syndesis.connector.sql.common.StatementType;
import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;

public final class SqlConnectorCustomizer implements ComponentProxyCustomizer {

    private Map<String, Integer> jdbcTypeMap;
    private static final Logger LOGGER = LoggerFactory.getLogger(SqlConnectorCustomizer.class);
    private String autoIncrementColumnName;
    private boolean isRetrieveGeneratedKeys;
    private StatementType statementType;

    private boolean isBatch;
    private boolean isRaiseErrorOnNotFound;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        component.setBeforeProducer(this::doBeforeProducer);
        component.setAfterProducer(this::doAfterProducer);
        initJdbcMap(options);
    }

    @SuppressWarnings("unchecked")
    private void doBeforeProducer(Exchange exchange) {
        final Message in = exchange.getIn();

        List<String> jsonBeans = null;
        if (in.getBody() instanceof List) {
            jsonBeans = in.getBody(List.class);
        } else if (in.getBody(String.class) != null) {
            String body = in.getBody(String.class);
            if (JsonUtils.isJsonArray(body)) {
                try {
                    jsonBeans = JsonUtils.arrayToJsonBeans(Json.reader().readTree(body));
                } catch (IOException e) {
                    throw SyndesisConnectorException.wrap(SqlErrorCategory.SQL_DATA_ACCESS_ERROR, e);
                }
            } else if (JsonUtils.isJson(body)) {
                jsonBeans = Collections.singletonList(body);
            }
        }

        if (ObjectHelper.isNotEmpty(jsonBeans) && !jdbcTypeMap.isEmpty()) {
            if (isBatch) {
                final List<Map<String, SqlParameterValue>> sqlParametersValues = new ArrayList<>();
                for (String jsonBean : jsonBeans) {
                    sqlParametersValues.add(JSONBeanUtil.parseSqlParametersFromJSONBean(jsonBean, jdbcTypeMap));
                }
                exchange.getIn().setBody(sqlParametersValues);
            } else {
                final Map<String, SqlParameterValue> sqlParametersValues = JSONBeanUtil.parseSqlParametersFromJSONBean(jsonBeans.get(0), jdbcTypeMap);
                exchange.getIn().setBody(sqlParametersValues);
            }
        }
        if (isRetrieveGeneratedKeys) {
            exchange.getIn().setHeader(CamelSqlConstants.SQL_RETRIEVE_GENERATED_KEYS, true);
        }
    }

    private void doAfterProducer(Exchange exchange) {

        if (exchange.getException()!=null) {
            throw SyndesisConnectorException.wrap(
                    SqlErrorCategory.SQL_CONNECTOR_ERROR, exchange.getException());
        }
        final Message in = exchange.getIn();

        //converting SQL Map or List results to JSON Beans
        List<String> list = null;
        if (isRetrieveGeneratedKeys) {
            list = JSONBeanUtil.toJSONBeansFromHeader(in, autoIncrementColumnName);
        } else {
            list = JSONBeanUtil.toJSONBeans(in);
        }
        if (list != null && !list.isEmpty()) {
            in.setBody(list);
        }
        if (isRaiseErrorOnNotFound && !isRecordsFound(in))  {
            String detailedMsg = "SQL " + statementType.name() + " did not " + statementType +  " any records";
            throw new SyndesisConnectorException(SqlErrorCategory.SQL_ENTITY_NOT_FOUND_ERROR, detailedMsg);
        }
    }

    private boolean isRecordsFound(Message in) {

        switch (statementType) {

            case SELECT:
                Integer rowCount = (Integer) in.getHeader(CamelSqlConstants.SQL_ROW_COUNT);
                if (rowCount.intValue() > 0) {
                    return true;
                }
                break;

            case UPDATE:
            case DELETE:
            case INSERT:
                Integer updateCount = (Integer) in.getHeader(CamelSqlConstants.SQL_UPDATE_COUNT);
                if (updateCount.intValue() > 0) {
                	return true;
                }
                break;

        }
        return false;
    }

    private void initJdbcMap(Map<String, Object> options) {
        if (jdbcTypeMap == null) {

            isBatch = ConnectorOptions
                    .extractOptionAndMap(options, "batch", Boolean::valueOf, false);
            isRaiseErrorOnNotFound = ConnectorOptions
                    .extractOptionAndMap(options, "raiseErrorOnNotFound", Boolean::valueOf, false);

            final String sql =  ConnectorOptions.extractOption(options, "query");
            final DataSource dataSource = ConnectorOptions.extractOptionAsType(
                options, "dataSource", DataSource.class);

            final Map<String, Integer> tmpMap = new HashMap<>();
            try (Connection connection = dataSource.getConnection()) {
                DbMetaDataHelper dbHelper = new DbMetaDataHelper(connection);
                final String defaultSchema = dbHelper.getDefaultSchema(ConnectorOptions.extractOption(options, "user", ""));
                final String schemaPattern = ConnectorOptions.extractOption(options, "schema", defaultSchema);

                SqlStatementMetaData statementInfo = new SqlStatementParser(connection, schemaPattern, sql).parse();
                for (SqlParam sqlParam: statementInfo.getInParams()) {
                    tmpMap.put(sqlParam.getName(), sqlParam.getJdbcType().getVendorTypeNumber());
                }
                if (statementInfo.getAutoIncrementColumnName() != null) {
                    isRetrieveGeneratedKeys = true;
                    autoIncrementColumnName = statementInfo.getAutoIncrementColumnName();
                }

                statementInfo.setBatch(isBatch);
                isBatch = statementInfo.isVerifiedBatchUpdateMode();
                statementType = statementInfo.getStatementType();
                options.put("batch", isBatch);

            } catch (SQLException e){
                LOGGER.error(e.getMessage(),e);
            }

            jdbcTypeMap = tmpMap;
        }
    }

}
