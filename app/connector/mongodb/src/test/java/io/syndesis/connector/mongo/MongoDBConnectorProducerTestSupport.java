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
package io.syndesis.connector.mongo;

import com.mongodb.client.MongoCollection;
import io.syndesis.connector.mongo.embedded.EmbedMongoConfiguration;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class MongoDBConnectorProducerTestSupport extends MongoDBConnectorTestSupport{

    protected MongoCollection<Document> collection;

    public abstract String getCollectionName();

    @BeforeEach
    public void before(){
        collection = EmbedMongoConfiguration.DATABASE.getCollection(getCollectionName());
    }

    @AfterEach
    public void after(){
        collection.drop();
    }

}
