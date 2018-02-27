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
import javax.management.ObjectName;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.Service;
import org.apache.camel.api.management.ManagedAttribute;
import org.apache.camel.api.management.ManagedResource;
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

    @ManagedAttribute
    public Long getStartTimestamp() {
        return camelContext.getManagedCamelContext().getStartTimestamp().getTime();
    }

    @ManagedAttribute
    public Long getResetTimestamp() {
        final Date resetTimestamp = camelContext.getManagedCamelContext().getResetTimestamp();
        return resetTimestamp == null ? null : resetTimestamp.getTime();
    }

    @ManagedAttribute
    public Long getLastExchangeCompletedTimestamp() {
        final Date timestamp = camelContext.getManagedCamelContext()
                .getLastExchangeCompletedTimestamp();
        return timestamp == null ? null : timestamp.getTime();
    }

    @ManagedAttribute
    public Long getLastExchangeFailureTimestamp() {
        final Date timestamp = camelContext.getManagedCamelContext()
                .getLastExchangeFailureTimestamp();
        return timestamp == null ? null : timestamp.getTime();
    }

    @Override
    public void start() throws Exception {
        // register mbean
        final String contextName = camelContext.getName();
        final String name = String.format("io.syndesis.camel:context=%s,type=context,name=\"%s\"", contextName, contextName);
        final ObjectName instance = ObjectName.getInstance(name);

        camelContext.getManagementStrategy().manageNamedObject(this, instance);
        LOG.info("Registered mbean {}", instance);
    }

    @Override
    public void stop() throws Exception {
        // unregister mbean
        camelContext.getManagementStrategy().unmanageObject(this);
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
