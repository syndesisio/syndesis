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
package io.syndesis.connector.tradeinsight;

public class TradeContext {
    private int confidenceScore;
    private String tradeWindowStart;
    private String tradeWindowEnd;

    public TradeContext() {
    }

    public int getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(int confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getTradeWindowStart() {
        return tradeWindowStart;
    }

    public void setTradeWindowStart(String tradeWindowStart) {
        this.tradeWindowStart = tradeWindowStart;
    }

    public String getTradeWindowEnd() {
        return tradeWindowEnd;
    }

    public void setTradeWindowEnd(String tradeWindowEnd) {
        this.tradeWindowEnd = tradeWindowEnd;
    }
}
