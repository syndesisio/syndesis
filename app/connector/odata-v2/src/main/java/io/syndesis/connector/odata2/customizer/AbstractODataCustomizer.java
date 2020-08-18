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
package io.syndesis.connector.odata2.customizer;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.syndesis.connector.odata2.ODataConstants;
import io.syndesis.connector.odata2.customizer.json.ODataEntrySerializer;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.Message;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.ep.feed.ODataFeed;

public abstract class AbstractODataCustomizer implements ComponentProxyCustomizer, CamelContextAware, ODataConstants {

    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String EMPTY_JSON = OPEN_BRACE + CLOSE_BRACE;

    private boolean split;

    static {
        SimpleModule module = new SimpleModule(ODataEntry.class.getSimpleName(),
            new Version(1, 0, 0, null, null, null));
        module.addSerializer(new ODataEntrySerializer());
        OBJECT_MAPPER.registerModule(module);
    }

    private CamelContext camelContext;

    @Override
    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    @Override
    public CamelContext getCamelContext() {
        return this.camelContext;
    }

    public boolean isSplit() {
        return this.split;
    }

    protected void setSplit(boolean split) {
        this.split = split;
    }

    private void addToBody(Message in, String payload) {
        if (isSplit()) {
            in.setBody(payload);
        } else {
            List<String> resultList = new ArrayList<>();
            resultList.add(payload);
            in.setBody(resultList);
        }
    }

    protected void convertMessageToJson(Message in) throws JsonProcessingException, EdmException {
        if (in.getBody() == null) {
            addToBody(in, EMPTY_JSON);
            return;
        }

        Object item = in.getBody(Object.class);
        if (item instanceof ODataFeed) {
            //
            // If the results have not been split and returned as a
            // ODataFeed then split it up into a recognisable list
            //
            ODataFeed oDataFeed = (ODataFeed) item;
            List<ODataEntry> entities = oDataFeed.getEntries();
            if (entities == null || entities.isEmpty()) {
                addToBody(in, EMPTY_JSON);
                return;
            }

            List<String> resultList = new ArrayList<>();
            for (ODataEntry entity : entities) {
                if (oDataFeed.getFeedMetadata().getInlineCount() != null) {
                    //
                    // If $inlinecount was set to true in the query then
                    // need to include the count in the entities
                    //
                    entity.getProperties().put(RESULT_COUNT, oDataFeed.getFeedMetadata().getInlineCount());
                }

                resultList.add(OBJECT_MAPPER.writeValueAsString(entity));
            }
            in.setBody(resultList);

        } else {
            //
            // Need to keep aligned with the json-schema presented
            // by ODataMetadataRetrieval.
            // - If split option set to true then json-schema will be entity
            // - If split option set to false then json-schema will be array
            //
            // Therefore, return the data in equivalent json structures.
            //
            addToBody(in, OBJECT_MAPPER.writeValueAsString(item));
        }
    }
}
