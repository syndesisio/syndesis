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

import io.syndesis.core.KeyGenerator;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.processor.DefaultExchangeFormatter;
import org.apache.camel.spi.InterceptStrategy;

import static io.syndesis.integration.runtime.util.JsonSupport.toJsonObject;

public class IntegrationLoggingInterceptStrategy implements InterceptStrategy {
    private static final DefaultExchangeFormatter FORMATTER = new DefaultExchangeFormatter();

    @SuppressWarnings({"PMD.SystemPrintln", "PMD.AvoidCatchingGenericException"})
    @Override
    public Processor wrapProcessorInInterceptors(CamelContext context, ProcessorDefinition<?> definition, Processor target, Processor nextTarget) throws Exception {
        if (!definition.hasCustomIdAssigned()) {
            // skip over processors with a generated id
            return target;
        }
        return exchange -> {
            String id = KeyGenerator.createKey();
            long startedAt = System.nanoTime();
            try {
                target.process(exchange);
            } catch (RuntimeException e) {
                exchange.setException(e);
            } finally {
                // currentTimeMillis is not monotonic, nanoTime likely is
                long duration = System.nanoTime() - startedAt;
                System.out.println(toJsonObject(
                    "exchange", exchange.getExchangeId(),
                    "step", definition.getId(),
                    "id", id,
                    "duration", duration,
                    "failure", failure(exchange)));
            }
        };
    }


    private static String failure(Exchange exchange) {
        if( exchange.isFailed() ) {
            if( exchange.getException()!=null ) {
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
