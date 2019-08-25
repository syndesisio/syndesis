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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import io.syndesis.common.model.integration.Step;
import io.syndesis.common.util.ErrorCategory;
import io.syndesis.common.util.SyndesisConnectorException;
import io.syndesis.connector.sql.common.JSONBeanUtil;
import io.syndesis.connector.sql.util.SqlConnectorTestSupport;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@SuppressWarnings({"PMD.SignatureDeclareThrowsException", "PMD.JUnitTestsShouldIncludeAssert"})
@RunWith(Parameterized.class)
public class SqlConnectorQueryTest extends SqlConnectorTestSupport {

    private final String sqlQuery;
    private final List<Map<String, String[]>> expectedResults;
    private final Map<String, Object> parameters;

    public SqlConnectorQueryTest(String sqlQuery, List<Map<String, String[]>> expectedResults, Map<String, Object> parameters) {
        this.sqlQuery = sqlQuery;
        this.expectedResults = expectedResults;
        this.parameters = parameters;
    }

    @Override
    protected List<String> setupStatements() {
        return Arrays.asList("CREATE TABLE ADDRESS (street VARCHAR(255), number INTEGER)",
                                "INSERT INTO ADDRESS VALUES ('East Davie Street', 100)",
                                "INSERT INTO ADDRESS VALUES ('Am Treptower Park', 75)",
                                "INSERT INTO ADDRESS VALUES ('Werner-von-Siemens-Ring', 14)");
    }

    @Override
    protected List<String> cleanupStatements() {
        return Collections.singletonList("DROP TABLE ADDRESS");
    }

    @Override
    protected List<Step> createSteps() {
        return Arrays.asList(
            newSimpleEndpointStep(
                "direct",
                builder -> builder.putConfiguredProperty("name", "start")),
            newSqlEndpointStep(
                "sql-connector",
                builder -> builder.putConfiguredProperty("query", sqlQuery)
                                  .putConfiguredProperty("raiseErrorOnNotFound", "true")),
            newSimpleEndpointStep(
                "log",
                builder -> builder.putConfiguredProperty("loggerName", "test"))
        );
    }

    // **************************
    // Parameters
    // **************************

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "SELECT * FROM ADDRESS", 
                		Arrays.asList(
                				Collections.singletonMap("NUMBER", new String[] { "100", "75", "14" }),
                				Collections.singletonMap("STREET", new String[] { "East Davie Street", "Am Treptower Park", "Werner-von-Siemens-Ring" })), 
                		Collections.emptyMap()},
                { "SELECT * FROM ADDRESS WHERE number = 14", 
                        Arrays.asList(
                        		Collections.singletonMap("NUMBER", new String[] { "14" }),
                        		Collections.singletonMap("STREET", new String[] { "Werner-von-Siemens-Ring" })), 
                        Collections.emptyMap()},
                { "SELECT street FROM ADDRESS WHERE number = :#number", 
                        Collections.singletonList(Collections.singletonMap("STREET", new String[]{ "East Davie Street" })), 
                        Collections.singletonMap("number", "100")},
                // Causes a SyndesisConnectorException since no such record exists and 
                // isRaiseErrorOnNotFound is set to true
                { "SELECT * FROM ADDRESS WHERE number = 0",
                        Arrays.asList(
                        		new HashMap<String,String>() {
									private static final long serialVersionUID = 1L;
									{
                        				put("EXCEPTION_MESSAGE", "SQL SELECT did not SELECT any records" );
                        				put("EXCEPTION_CATEGORY", "SQL_ENTITY_NOT_FOUND_ERROR");
                        			}
                        		}
                        	),
        		        Collections.emptyMap()
                },
                // Causes a runtime exception for bad SQL grammar not caught by Camel, 
                // for now best we can do is to classify it as a SERVER_ERROR, so let's check 
                // this happens.
                { "INSERT INTO ADDRESS VALUES (4, 'angerloseweg', '11')",
                		Arrays.asList(
                    		new HashMap<String,String>() {
								private static final long serialVersionUID = 1L;
								{
                    				put("EXCEPTION_MESSAGE", "PreparedStatementCallback; bad SQL grammar []; nested exception is java.sql.SQLSyntaxErrorException: The number of values assigned is not the same as the number of specified or implied columns." );
                    				put("EXCEPTION_CATEGORY", ErrorCategory.SERVER_ERROR);
                    			}
                    		}
                    	),
                		Collections.emptyMap()
                },
                { "DELETE FROM ADDRESS WHERE number = 14",
                	    null,
	    		        Collections.emptyMap()
                }
        });
    }

    // **************************
    // Tests
    // **************************

    @Test
    public void sqlConnectorQueryTest() {
        String body;
        if (parameters.isEmpty()) {
            body = null;
        } else {
            body = JSONBeanUtil.toJSONBean(parameters);
        }

        List<?> results = null;

        try {

	        results = template.requestBody("direct:start", body, List.class);

        } catch (Throwable e) {

            //check if this was an expected error
            SyndesisConnectorException sce = 
                    SyndesisConnectorException.from(e);
            Assert.assertEquals(expectedResults.get(0).get("EXCEPTION_MESSAGE"),  sce.getMessage());
            Assert.assertEquals(expectedResults.get(0).get("EXCEPTION_CATEGORY"), sce.getCategory());
            return;
        }

        if (expectedResults == null) {
        	Assert.assertNull(results);
        } else {
            List<Properties> jsonBeans = results.stream()
                    .map(Object::toString)
                    .map(JSONBeanUtil::parsePropertiesFromJSONBean)
                    .collect(Collectors.toList());
    
            Assert.assertEquals(expectedResults.isEmpty(), jsonBeans.isEmpty());
    
            for (Map<String, String[]> result : expectedResults) {
                for (Map.Entry<String, String[]> resultEntry : result.entrySet()) {
                    validateProperty(jsonBeans, resultEntry.getKey(), resultEntry.getValue());
                }
            }
        }
    }
}
