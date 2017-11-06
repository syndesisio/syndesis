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
package io.syndesis.connector.daytrade;

public class Trade {

    private String action;
    private String expiration;
    private int limit;
    private int quantity;
    private String ticker;

    public Trade() {
    }

    public Trade(final String ticker, final int quantity, final String action, final int limit,
        final String expiration) {
        this.limit = limit;
        this.ticker = ticker;
        this.quantity = quantity;
        this.action = action;
        this.expiration = expiration;
    }

    public String getAction() {
        return action;
    }

    public String getExpiration() {
        return expiration;
    }

    public int getLimit() {
        return limit;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getTicker() {
        return ticker;
    }

    public void setAction(final String action) {
        this.action = action;
    }

    public void setExpiration(final String expiration) {
        this.expiration = expiration;
    }

    public void setLimit(final int limit) {
        this.limit = limit;
    }

    public void setQuantity(final int quantity) {
        this.quantity = quantity;
    }

    public void setTicker(final String ticker) {
        this.ticker = ticker;
    }

    @Override
    public String toString() {
        return "\nlimit  = " + limit + "\nticker = " + ticker + "\nquantity = " + quantity + "\nexpires = "
            + expiration + "\naction = " + action;
    }
}
