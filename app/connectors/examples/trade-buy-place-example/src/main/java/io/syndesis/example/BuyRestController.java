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
package io.syndesis.example;

import java.util.Calendar;

import io.syndesis.connector.tradeinsight.OrderDetail;
import io.syndesis.connector.tradeinsight.Suggestion;
import io.syndesis.connector.tradeinsight.TradeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Used for simulating a rest service which we can run locally inside Spring Boot
 */
@RestController
public class BuyRestController {

    private static final Logger LOG = LoggerFactory.getLogger(BuyRestController.class);

    @RequestMapping(value = "/trade-insight/buy", method = RequestMethod.GET, produces = "application/json")
    public Suggestion buy() {
        LOG.info("Recommending a suggestion");
        return buyData();
    }

    private Suggestion buyData() {
        Suggestion buy = new Suggestion();
        buy.setOrder("BUY BABO");
        TradeContext tc = new TradeContext();
        tc.setConfidenceScore(10);
        Calendar window = Calendar.getInstance();
        tc.setTradeWindowStart(window.getTime().toString());
        window.add(Calendar.HOUR, 5);
        tc.setTradeWindowEnd(window.getTime().toString());
        OrderDetail order = new OrderDetail();
        order.setNumShares(1000);
        order.setTargetPrice(10);
        buy.setContext(tc);
        buy.setDetail(order);
        return buy;
    }

}
