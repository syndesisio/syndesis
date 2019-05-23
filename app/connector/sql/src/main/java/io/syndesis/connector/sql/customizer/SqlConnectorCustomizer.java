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

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.syndesis.common.util.Json;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.connector.sql.common.JSONBeanUtil;
import io.syndesis.connector.sql.common.SqlParam;
import io.syndesis.connector.sql.common.SqlStatementMetaData;
import io.syndesis.connector.sql.common.SqlStatementParser;
import io.syndesis.connector.sql.common.StatementType;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.sql.SqlConstants;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.SqlParameterValue;

public final class SqlConnectorCustomizer implements ComponentProxyCustomizer {

    private Map<String, Integer> jdbcTypeMap;
    private static final Logger LOGGER = LoggerFactory.getLogger(SqlConnectorCustomizer.class);
    private String autoIncrementColumnName;
    private boolean isRetrieveGeneratedKeys;

    private boolean batch;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        component.setBeforeProducer(this::doBeforeProducer);
        component.setAfterProducer(this::doAfterProducer);
        initJdbcMap(options);
    }

    @SuppressWarnings("unchecked")
    private void doBeforeProducer(Exchange exchange) throws IOException {
        final Message in = exchange.getIn();

        List<String> jsonBeans = null;
        if (in.getBody() instanceof List) {
            jsonBeans = in.getBody(List.class);
        } else if (in.getBody(String.class) != null) {
            String body = in.getBody(String.class);
            if (JsonUtils.isJsonArray(body)) {
                jsonBeans = JsonUtils.arrayToJsonBeans(Json.reader().readTree(body));
            } else if (JsonUtils.isJson(body)) {
                jsonBeans = Collections.singletonList(body);
            }
        }

        if (ObjectHelper.isNotEmpty(jsonBeans) && !jdbcTypeMap.isEmpty()) {
            if (batch) {
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
            exchange.getIn().setHeader(SqlConstants.SQL_RETRIEVE_GENERATED_KEYS, true);
        }
    }

    private void doAfterProducer(Exchange exchange) {
        final Message in = exchange.getIn();
        //converting SQL Map or List results to JSON Beans
        List<String> list = null;
        if (isRetrieveGeneratedKeys) {
            list = JSONBeanUtil.toJSONBeansFromHeader(in, autoIncrementColumnName);
        } else {
            list = JSONBeanUtil.toJSONBeans(in);
        }
        if (list != null) {
            in.setBody(list);
        }
    }

    private void initJdbcMap(Map<String, Object> options) {
        if (jdbcTypeMap == null) {
            final String sql =  String.valueOf(options.get("query"));
            final DataSource dataSource = (DataSource) options.get("dataSource");

            final Map<String, Integer> tmpMap = new HashMap<>();
            try (Connection connection = dataSource.getConnection()) {

                SqlStatementMetaData statementInfo = new SqlStatementParser(connection, null, sql).parse();
                for (SqlParam sqlParam: statementInfo.getInParams()) {
                    tmpMap.put(sqlParam.getName(), sqlParam.getJdbcType().getVendorTypeNumber());
                }
                if (statementInfo.getAutoIncrementColumnName() != null) {
                    isRetrieveGeneratedKeys = true;
                    autoIncrementColumnName = statementInfo.getAutoIncrementColumnName();
                }

                batch = statementInfo.hasInputParams() && statementInfo.getStatementType() != StatementType.SELECT;
                options.put("batch", batch);
            } catch (SQLException e){
                LOGGER.error(e.getMessage(),e);
            }

            jdbcTypeMap = tmpMap;
        }
    }

}
