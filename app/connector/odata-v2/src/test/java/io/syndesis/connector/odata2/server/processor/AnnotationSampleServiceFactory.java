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
package io.syndesis.connector.odata2.server.processor;

import io.syndesis.connector.odata2.server.model.Manufacturer;
import org.apache.olingo.odata2.annotation.processor.api.AnnotationServiceFactory;
import org.apache.olingo.odata2.api.ODataCallback;
import org.apache.olingo.odata2.api.ODataDebugCallback;
import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.ODataServiceFactory;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.processor.ODataErrorCallback;
import org.apache.olingo.odata2.api.processor.ODataErrorContext;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Taken from the tutorial:
 * http://olingo.apache.org/doc/odata2/tutorials/OlingoV2BasicClientSample.html
 */
public class AnnotationSampleServiceFactory extends ODataServiceFactory {

    /**
     * Instance holder for all annotation relevant instances which should be used as singleton
     * instances within the ODataApplication (ODataService)
     */
    private static class AnnotationInstances {
        final static String MODEL_PACKAGE = Manufacturer.class.getPackage().getName();
        final static ODataService ANNOTATION_ODATA_SERVICE;

        static {
            try {
                ANNOTATION_ODATA_SERVICE = AnnotationServiceFactory.createAnnotationService(MODEL_PACKAGE);
            } catch (ODataApplicationException ex) {
                throw new RuntimeException("Exception during sample data generation.", ex);
            } catch (ODataException ex) {
                throw new RuntimeException("Exception during data source initialization generation.", ex);
            }
        }
    }

    @Override
    public ODataService createService(final ODataContext context) throws ODataException {
        // Edm via Annotations and ListProcessor via AnnotationDS with AnnotationsValueAccess
        return AnnotationInstances.ANNOTATION_ODATA_SERVICE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ODataCallback> T getCallback(final Class<T> callbackInterface) {
        return (T) (callbackInterface.isAssignableFrom(ScenarioErrorCallback.class)
            ? new ScenarioErrorCallback() : callbackInterface.isAssignableFrom(ODataDebugCallback.class)
            ? new ScenarioDebugCallback() : super.getCallback(callbackInterface));
    }

    /*
     * Helper classes and methods
     */
    private static final class ScenarioDebugCallback implements ODataDebugCallback {

        @Override
        public boolean isDebugEnabled() {
            return true;
        }
    }

    private static class ScenarioErrorCallback implements ODataErrorCallback {
        private static final Logger LOG = LoggerFactory.getLogger(ScenarioErrorCallback.class);

        @Override
        public ODataResponse handleError(final ODataErrorContext context) throws ODataApplicationException {
            if (context.getHttpStatus() == HttpStatusCodes.INTERNAL_SERVER_ERROR) {
                LOG.error("Internal Server Error", context.getException());
            }

            return EntityProvider.writeErrorDocument(context);
        }
    }
}
