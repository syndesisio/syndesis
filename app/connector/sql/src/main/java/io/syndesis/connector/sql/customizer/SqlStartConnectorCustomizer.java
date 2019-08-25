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
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import io.syndesis.connector.sql.common.DbMetaDataHelper;

import io.syndesis.common.util.SyndesisConnectorException;

import io.syndesis.connector.sql.common.JSONBeanUtil;
import io.syndesis.connector.sql.common.SqlStatementMetaData;
import io.syndesis.connector.sql.common.SqlStatementParser;
import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.sql.SqlConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class SqlStartConnectorCustomizer implements ComponentProxyCustomizer {

    private boolean isInit;
    private static final Logger LOGGER = LoggerFactory.getLogger(SqlStartConnectorCustomizer.class);
    private String autoIncrementColumnName;
    private boolean isRetrieveGeneratedKeys;

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        component.setBeforeProducer(this::doBeforeProducer);
        component.setAfterProducer(this::doAfterProducer);
        init(options);
    }

    private void doBeforeProducer(Exchange exchange) {
        final String body = exchange.getIn().getBody(String.class);
        if (body != null) {
            final Properties properties = JSONBeanUtil.parsePropertiesFromJSONBean(body);
            exchange.getIn().setBody(properties);
        }
        if (isRetrieveGeneratedKeys) {
            exchange.getIn().setHeader(SqlConstants.SQL_RETRIEVE_GENERATED_KEYS, true);
        }
    }

    private void doAfterProducer(Exchange exchange) {
        Exception e = exchange.getException();
        if (e != null) {
            throw SyndesisConnectorException.wrap(SqlErrorCategory.SQL_CONNECTOR_ERROR, e);
        }
        final Message in = exchange.getIn();
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

    private void init(Map<String, Object> options) {
        if (!isInit) {
            final String sql =  ConnectorOptions.extractOption(options, "query");
            final DataSource dataSource = ConnectorOptions.extractOptionAsType(options, "dataSource", DataSource.class);
            try (Connection connection = dataSource.getConnection()) {
                DbMetaDataHelper dbHelper = new DbMetaDataHelper(connection);
                final String defaultSchema = dbHelper.getDefaultSchema(ConnectorOptions.extractOption(options, "user", ""));
                final String schemaPattern = ConnectorOptions.extractOption(options, "schema", defaultSchema);

                SqlStatementMetaData statementInfo = new SqlStatementParser(connection, schemaPattern, sql).parse();
                if (statementInfo.getAutoIncrementColumnName() != null) {
                    isRetrieveGeneratedKeys = true;
                    autoIncrementColumnName = statementInfo.getAutoIncrementColumnName();
                }
            } catch (SQLException e){
                LOGGER.error(e.getMessage(),e);
            }
            isInit = true;
        }
    }
}
