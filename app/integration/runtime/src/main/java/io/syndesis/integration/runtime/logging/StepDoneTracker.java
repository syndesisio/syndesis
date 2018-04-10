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
package io.syndesis.integration.runtime.logging;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.camel.AsyncCallback;
import org.apache.camel.AsyncProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.processor.DefaultExchangeFormatter;
import org.apache.camel.util.AsyncProcessorHelper;

import static io.syndesis.integration.runtime.util.JsonSupport.toJsonObject;

/**
 * Processor used to track the end of a Syndesis step.
 */
public class StepDoneTracker implements AsyncProcessor {

    public static final StepDoneTracker INSTANCE = new StepDoneTracker();
    private static final DefaultExchangeFormatter FORMATTER = new DefaultExchangeFormatter();

    @Override
    public void process(Exchange exchange) throws Exception {
        AsyncProcessorHelper.process(this, exchange);
    }

    @Override
    public boolean process(Exchange exchange, AsyncCallback callback) {
        done(exchange);
        return true;
    }

    @SuppressWarnings("PMD")
    public static void done(Exchange exchange) {
        StepStartTracker startTracker = (StepStartTracker) exchange.removeProperty(IntegrationLoggingConstants.STEP_START_TRACKER);
        if (startTracker != null) {
            final long duration = System.nanoTime() - startTracker.getStartedAt();
            final String exchangeId = exchange.getProperty(IntegrationLoggingConstants.EXCHANGE_ID, exchange.getExchangeId(), String.class);

            System.out.println(toJsonObject(
                "exchange", exchangeId,
                "step", startTracker.getStep(),
                "id", startTracker.getId(),
                "duration", duration,
                "failure", failure(exchange)));
        }
    }

    private static String failure(Exchange exchange) {
        if (exchange.isFailed()) {
            if (exchange.getException() != null) {
                return toString(exchange.getException());
            }
            return FORMATTER.format(exchange);
        }
        return null;
    }

    private static String toString(Throwable exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        pw.close();
        return sw.toString();
    }
}
