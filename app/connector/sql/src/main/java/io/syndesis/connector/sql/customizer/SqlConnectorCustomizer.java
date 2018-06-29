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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import io.syndesis.connector.sql.common.JSONBeanUtil;
import io.syndesis.connector.sql.common.SqlParam;
import io.syndesis.connector.sql.common.SqlStatementMetaData;
import io.syndesis.connector.sql.common.SqlStatementParser;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.SqlParameterValue;

public final class SqlConnectorCustomizer implements ComponentProxyCustomizer {

    Map<String, Object> options;
    Map<String, Integer> jdbcTypeMap;
    private static final Logger LOGGER = LoggerFactory.getLogger(SqlConnectorCustomizer.class);

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        component.setBeforeProducer(this::doBeforeProducer);
        component.setAfterProducer(this::doAfterProducer);
        this.options = options;
    }

    private void doBeforeProducer(Exchange exchange) {
        if (jdbcTypeMap==null) {
            initJdbcMap();
        }
        final String body = exchange.getIn().getBody(String.class);
        if (body != null) {
            final Map<String,SqlParameterValue> sqlParametersValues = JSONBeanUtil.parseSqlParametersFromJSONBean(body, jdbcTypeMap);
            exchange.getIn().setBody(sqlParametersValues);
        }
    }

    private void doAfterProducer(Exchange exchange) {
        final String jsonBean;

        if (exchange.getIn().getBody(List.class) != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> maps = exchange.getIn().getBody(List.class);
            if (maps.isEmpty()) {
                throw new IllegalStateException("Got an empty collection");
            }

            //Only grabbing the first record (map) in the list
            jsonBean = JSONBeanUtil.toJSONBean(maps.get(0));
        } else {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = exchange.getIn().getBody(Map.class);
            jsonBean = JSONBeanUtil.toJSONBean(body);
        }

        exchange.getIn().setBody(jsonBean);
    }

    private void initJdbcMap() {

        final String sql =  String.valueOf(options.get("query"));
        final DataSource dataSource = (DataSource) options.get("dataSource");

        final Map<String, Integer> tmpMap = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {

            SqlStatementMetaData md = new SqlStatementParser(connection, null, sql).parse();
            for (SqlParam sqlParam: md.getInParams()) {
                tmpMap.put(sqlParam.getName(), sqlParam.getJdbcType().getVendorTypeNumber());
            }
        } catch (SQLException e){
            LOGGER.error(e.getMessage(),e);
        }
        jdbcTypeMap = tmpMap;
    }

}
