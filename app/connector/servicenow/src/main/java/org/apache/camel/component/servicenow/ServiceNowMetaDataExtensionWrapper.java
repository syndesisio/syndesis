package org.apache.camel.component.servicenow;

import org.apache.camel.CamelContext;

/**
 * Wrapper needed because the constructor of this
 * extension is not available outside the package.
 */
public final class ServiceNowMetaDataExtensionWrapper {

    private ServiceNowMetaDataExtensionWrapper(){}

    public static ServiceNowMetaDataExtension getServiceNowMetaDataExtension(CamelContext context) {
        return new ServiceNowMetaDataExtension(context);
    }
}
