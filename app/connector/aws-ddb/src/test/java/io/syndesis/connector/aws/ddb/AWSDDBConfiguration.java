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
package io.syndesis.connector.aws.ddb;


import com.amazonaws.regions.Regions;

@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")

/**
 * To be able to run these integration tests you have to provide valid credentials, region
 * and a valid table name.
 *
 * These tests (at the moment) do write and read contents on your tables, so make sure
 * you use a table specifically for testing.
 */
public class AWSDDBConfiguration {

    protected static final String ACCESSKEY_VALUE = "INVALID_KEY";
    protected static final String SECRETKEY_VALUE = "INVALID_KEY";
    protected static final String REGION_VALUE = Regions.EU_WEST_2.name();
    protected static final String TABLENAME_VALUE = "TestTable";

    protected static final String ACCESSKEY = "accessKey";
    protected static final String SECRETKEY = "secretKey";
    protected static final String REGION = "region";
    protected static final String TABLENAME = "tableName";
    protected static final String ELEMENT = "element";

    protected static final Long randomId = System.currentTimeMillis();

    //TODO change this to your table constraints
    protected static final String ELEMENT_VALUE = "{\"clave\":\"" + randomId + "\"}";
    //TODO change this to your table constraints
}
