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
package io.syndesis.server.api.generator.soap.parser;

import java.io.IOException;

import javax.wsdl.xml.WSDLLocator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public final class Locator implements WSDLLocator {

    private static final Logger LOG = LoggerFactory.getLogger(Locator.class);

    private final InputSource baseInputSource;
    private final String baseURI;
    private String lastImportURI;

    private final EntityResolver resolver = new Resolver();

    public Locator(final String wsdlURL, final InputSource inputSource) {
        baseURI = wsdlURL;
        baseInputSource = inputSource;
    }

    @Override
    public void close() {
        // nothing to close
    }

    @Override
    public InputSource getBaseInputSource() {
        return baseInputSource;
    }

    @Override
    public String getBaseURI() {
        return baseURI;
    }

    @Override
    public InputSource getImportInputSource(final String parentLocation, final String importLocation) {
        lastImportURI = importLocation;
        try {
            return resolver.resolveEntity(null, importLocation);
        } catch (SAXException | IOException e) {
            LOG.warn("Unable to resolve: {} from {}, will use platform default", importLocation, parentLocation);
            LOG.debug("The exception while resolving: {} from {} is", importLocation, parentLocation, e);

            return new InputSource(importLocation);
        }
    }

    @Override
    public String getLatestImportURI() {
        return lastImportURI;
    }

}
