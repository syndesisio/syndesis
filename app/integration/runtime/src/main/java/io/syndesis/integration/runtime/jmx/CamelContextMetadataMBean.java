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
package io.syndesis.integration.runtime.jmx;

import java.util.Date;
import java.util.Optional;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.Service;
import org.apache.camel.api.management.ManagedAttribute;
import org.apache.camel.api.management.ManagedCamelContext;
import org.apache.camel.api.management.ManagedResource;
import org.apache.camel.api.management.mbean.ManagedCamelContextMBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple mapping mbean to convert some {@link org.apache.camel.api.management.mbean.ManagedCamelContextMBean}
 * metadata fields to Prometheus metrics.
 *
 * @author dhirajsb
 */
@ManagedResource(description = "Managed Syndesis CamelContext")
public class CamelContextMetadataMBean implements Service, CamelContextAware {

    public static final Logger LOG = LoggerFactory.getLogger(CamelContextMetadataMBean.class);

    private CamelContext camelContext;

    private ManagedCamelContextMBean getManagedCamelContext() {
        ManagedCamelContext mcc = camelContext.getExtension(ManagedCamelContext.class);
        return Optional.of(mcc)
            .map(m -> m.getManagedCamelContext())
            .orElse(null);
    }

    @ManagedAttribute
    public Long getStartTimestamp() {
        ManagedCamelContextMBean mcc = getManagedCamelContext();
        return mcc == null ? null : mcc.getStartTimestamp().getTime();
    }

    @ManagedAttribute
    public Long getResetTimestamp() {
        ManagedCamelContextMBean mcc = getManagedCamelContext();
        final Date resetTimestamp = mcc == null ? null : mcc.getResetTimestamp();
        return resetTimestamp == null ? null : resetTimestamp.getTime();
    }

    @ManagedAttribute
    public Long getLastExchangeCompletedTimestamp() {
        ManagedCamelContextMBean mcc = getManagedCamelContext();
        final Date timestamp = mcc == null ? null : mcc.getLastExchangeCompletedTimestamp();
        return timestamp == null ? null : timestamp.getTime();
    }

    @ManagedAttribute
    public Long getLastExchangeFailureTimestamp() {
        ManagedCamelContextMBean mcc = getManagedCamelContext();
        final Date timestamp = mcc == null ? null : mcc.getLastExchangeFailureTimestamp();
        return timestamp == null ? null : timestamp.getTime();
    }

    @Override
    public void start() {
        // register mbean
        final String contextName = camelContext.getName();
        final String name = String.format("io.syndesis.camel:context=%s,type=context,name=\"%s\"", contextName, contextName);

        try {
            camelContext.getManagementStrategy().manageObject(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        LOG.info("Registered mbean {}", name);
    }

    @Override
    public void stop() {
        // unregister mbean
        try {
            camelContext.getManagementStrategy().unmanageObject(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    @Override
    public CamelContext getCamelContext() {
        return this.camelContext;
    }
}
