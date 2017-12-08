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
package io.syndesis.connector.odata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * OData resource path properties, including <code>keyPredicate</code>.
 * @see <a href="http://www.odata.org/getting-started/basic-tutorial/#queryData">Querying Data</a>
 * @author dhirajsb
 */
public class ODataResource {

    @JsonProperty("KeyPredicate")
    private String keyPredicate;

    // TODO add other OData path options

    @JsonCreator
    public ODataResource(@JsonProperty("KeyPredicate") String keyPredicate) {
        this.keyPredicate = keyPredicate;
    }

    public String getKeyPredicate() {
        return keyPredicate;
    }

    public void setKeyPredicate(final String keyPredicate) {
        this.keyPredicate = keyPredicate;
    }

    @Override
    public String toString() {
        return keyPredicate;
    }
}
