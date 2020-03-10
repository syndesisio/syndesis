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
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.Service;
import org.apache.camel.api.management.ManagedAttribute;
import org.apache.camel.api.management.ManagedCamelContext;
import org.apache.camel.api.management.ManagedResource;
import org.apache.camel.management.DefaultManagementObjectNameStrategy;
import org.apache.camel.spi.ManagementObjectNameStrategy;
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
        return camelContext.getExtension(ManagedCamelContext.class).getManagedCamelContext().getStartTimestamp().getTime();
    }

    @ManagedAttribute
    public Long getResetTimestamp() {
        final Date resetTimestamp = camelContext.getExtension(ManagedCamelContext.class).getManagedCamelContext().getResetTimestamp();
        return resetTimestamp == null ? null : resetTimestamp.getTime();
    }

    @ManagedAttribute
    public Long getLastExchangeCompletedTimestamp() {
        final Date timestamp = camelContext.getExtension(ManagedCamelContext.class).getManagedCamelContext()
                .getLastExchangeCompletedTimestamp();
        return timestamp == null ? null : timestamp.getTime();
    }

    @ManagedAttribute
    public Long getLastExchangeFailureTimestamp() {
        final Date timestamp = camelContext.getExtension(ManagedCamelContext.class).getManagedCamelContext()
                .getLastExchangeFailureTimestamp();
        return timestamp == null ? null : timestamp.getTime();
    }

    @Override
    public void start() {
        // register mbean
        final String contextName = camelContext.getName();
        final String name = String.format("io.syndesis.camel:context=%s,type=context,name=\"%s\"", contextName, contextName);
        final ObjectName instanceName;
        try {
            instanceName = ObjectName.getInstance(name);
        } catch (MalformedObjectNameException e) {
            throw new RuntimeCamelException(e);
        }
        final Object currentInstance = this;

        camelContext.getManagementStrategy().setManagementObjectNameStrategy(new DefaultManagementObjectNameStrategy() {
            @Override
            public ObjectName getObjectName(Object managedObject) throws MalformedObjectNameException {
                if (currentInstance.equals(managedObject)) {
                    return instanceName;
                } else {
                    return super.getObjectName(managedObject);
                }
            }
        });
        try {
            camelContext.getManagementStrategy().manageObject(this);
        } catch (Exception e) {
            throw new RuntimeCamelException(e);
        }
        ManagementObjectNameStrategy defaultNameStrategy = camelContext.getManagementStrategy().getManagementObjectNameStrategy();
        camelContext.getManagementStrategy().setManagementObjectNameStrategy(defaultNameStrategy);

        LOG.info("Registered mbean {}", instanceName);
    }

    @Override
    public void stop() {
        // unregister mbean
        try {
            camelContext.getManagementStrategy().unmanageObject(this);
        } catch (Exception e) {
            throw new RuntimeCamelException(e);
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
