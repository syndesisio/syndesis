/**
 * Copyright (C) 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.example;

import io.syndesis.connector.daytrade.Trade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Used for simulating a rest service which we can run locally inside Spring Boot
 */
@RestController
public class PlaceRestController {

    private static final Logger LOG = LoggerFactory.getLogger(PlaceRestController.class);

    @RequestMapping(value = "/day-trade/orders", method = RequestMethod.POST, consumes = "application/json")
    public void placeTrade(@RequestBody Trade trade) {
        LOG.info("Placing trade: {}", trade);
    }
}
