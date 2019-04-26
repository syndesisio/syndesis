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
package io.syndesis.connector.rest.swagger;

import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

final class XmlPayloadProcessor implements StreamFilter {

    private static final String NS_SYNDESIS_PARAMETERS = "http://syndesis.io/v1/swagger-connector-template/parameters";

    private static final String NS_SYNDESIS_REQUES = "http://syndesis.io/v1/swagger-connector-template/request";

    private static final QName QN_BODY = new QName(NS_SYNDESIS_REQUES, "body");

    private static final QName QN_PARAMETERS = new QName(NS_SYNDESIS_PARAMETERS, "parameters");

    private static final QName QN_REQUEST = new QName(NS_SYNDESIS_REQUES, "request");

    private final Map<String, Object> headers;

    private boolean inBody;

    private boolean inParameter;

    private boolean inParameters;

    private boolean inPayload;

    private boolean inRequest;

    private String parameterName;

    @SuppressWarnings("PMD.AvoidStringBufferField")
    private StringBuilder parameterValue;

    XmlPayloadProcessor(final Map<String, Object> headers) {
        this.headers = headers;
    }

    @Override
    public boolean accept(final XMLStreamReader reader) {
        final int eventType = reader.getEventType();

        switch (eventType) {
        case XMLStreamConstants.START_ELEMENT:
            processStartElement(reader);
            break;
        case XMLStreamConstants.END_ELEMENT:
            processEndElement(reader);
            break;
        case XMLStreamConstants.CHARACTERS:
        case XMLStreamConstants.CDATA:
            processText(reader);
            break;
        default:
            // do nothing
            break;
        }

        return (inRequest && inPayload) || !inRequest;
    }

    private void processEndElement(final XMLStreamReader reader) {
        final QName name = reader.getName();

        if (QN_REQUEST.equals(name)) {
            inRequest = false;
        } else if (QN_PARAMETERS.equals(name)) {
            inParameters = false;
            inParameter = false;
        } else if (QN_BODY.equals(name)) {
            inBody = false;
            inPayload = false;
        }

        if (inParameter) {
            inParameter = false;
            headers.put(parameterName, parameterValue.toString());
        }
    }

    private void processStartElement(final XMLStreamReader reader) {
        final QName name = reader.getName();

        if (QN_REQUEST.equals(name)) {
            inRequest = true;
        } else if (QN_BODY.equals(name)) {
            inBody = true;
        } else if (QN_PARAMETERS.equals(name)) {
            inParameters = true;
        }

        if (inParameters) {
            inParameter = !QN_PARAMETERS.equals(name);
            if (inParameter) {
                parameterName = name.getLocalPart();
                parameterValue = new StringBuilder();
            }
        }

        if (inBody) {
            inPayload = !QN_BODY.equals(name);
        }
    }

    private void processText(final XMLStreamReader reader) {
        if (inParameter) {
            parameterValue.append(reader.getText());
        }
    }

}
