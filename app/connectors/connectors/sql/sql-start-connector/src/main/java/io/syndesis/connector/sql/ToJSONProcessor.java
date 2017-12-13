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

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.syndesis.connector.sql.stored.JSONBeanUtil;

/**
 * Converts the body containing a Map containing with information of one single 
 * record to a JSON representation.
 * @author kstam
 *
 */
public class ToJSONProcessor implements Processor {
    private final static Logger LOGGER = LoggerFactory.getLogger(ToJSONProcessor.class);

    @SuppressWarnings("unchecked")
    @Override
    public void process(Exchange exchange) throws Exception {

         LOGGER.debug("Body in (Map): {}", exchange.getIn().getBody());
         String jsonBean = JSONBeanUtil.toJSONBean(exchange.getIn().getBody(Map.class));
         LOGGER.debug("Body out (JSON): {}", jsonBean);
         exchange.getIn().setBody(jsonBean);
    }

}
