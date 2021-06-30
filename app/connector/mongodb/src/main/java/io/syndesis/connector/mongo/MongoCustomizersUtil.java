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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import io.syndesis.connector.mongo.meta.FilterUtil;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toList;

public final class MongoCustomizersUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoCustomizersUtil.class);

    private MongoCustomizersUtil() {
    }

    /**
     * Used to convert any result MongoOperation (either {@link DeleteResult} or {@link UpdateResult}
     * to a {@link Long}
     */
    static void convertMongoResultToLong(Exchange exchange) {
        Message in = exchange.getIn();
        if (in.getBody() instanceof DeleteResult) {
            Long docsDeleted = in.getBody(DeleteResult.class).getDeletedCount();
            in.setBody(docsDeleted);
        } else if (in.getBody() instanceof UpdateResult) {
            Long docsUpdated = in.getBody(UpdateResult.class).getModifiedCount();
            in.setBody(docsUpdated);
        } else {
            LOGGER.warn("Impossible to convert the body, type was {}", in.getBody() == null ? null : in.getBody().getClass());
        }
    }

    /**
     * Used to convert any {@link Document} object to Json text list
     */
    static void convertMongoDocumentsToJsonTextList(Exchange exchange) {
        List<String> convertedToJson = new ArrayList<>();
        Message in = exchange.getIn();
        if (in.getBody() instanceof Document) {
            convertedToJson.add(in.getBody(Document.class).toJson());
        } else if (in.getBody() instanceof List) {
            @SuppressWarnings("unchecked")
            List<Document> list = in.getBody(List.class);
            convertedToJson.addAll(list.stream().map(Document::toJson).collect(toList()));
        } else {
            LOGGER.warn("Impossible to convert the body, type was {}", in.getBody() == null ? null : in.getBody().getClass());
            return;
        }
        in.setBody(convertedToJson);
    }

    /**
     * Utility method used to replace the adminDB parameter if it was not provided
     * by user
     */
    public static void replaceAdminDBIfMissing(Map<String, Object> params) {
        // Fallback admin database parameter
        if (!params.containsKey("adminDB")) {
            params.put("adminDB", params.get("database"));
        } else if ("".equals(params.get("adminDB"))) {
            params.replace("adminDB", params.get("database"));
        }
    }

    /**
     * Merge the filter expression with body and set the result as the new body
     */
    static void executeFilterComponent(Exchange exchange, String filter) {
        executeFilterComponent(exchange, filter, null);
    }

    /**
     * Merge the filter expression with body and set the result as an header
     */
    static void executeFilterComponent(Exchange exchange, String filter, String header) {
        // Merge the filter parameter expression with values coming in the input body
        final String body = exchange.getIn().getBody(String.class);
        if (filter != null) {
            String mergedFilter = FilterUtil.merge(filter, body);
            if (header == null) {
                // Put the merged criteria as the input body
                exchange.getIn().setBody(mergedFilter);
            } else {
                // Put the merged criteria as a header
                exchange.getIn().getHeaders().put(header, mergedFilter);
            }
        } else {
            throw new IllegalArgumentException("The operation requires the user to provide a filter expression!");
        }
    }
}
