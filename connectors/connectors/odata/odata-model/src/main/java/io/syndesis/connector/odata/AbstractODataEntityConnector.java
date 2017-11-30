package io.syndesis.connector.odata;

import java.io.InputStream;

import org.apache.camel.Message;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.serialization.ClientODataDeserializer;
import org.apache.olingo.client.api.serialization.ODataBinder;
import org.apache.olingo.commons.api.format.ContentType;

/**
 * Abstract base class for OData connectors that take an input entity.
 * @author dhirajsb
 */
public abstract class AbstractODataEntityConnector extends AbstractODataConnector {

    public AbstractODataEntityConnector(String componentName, String className) {
        super(componentName, className);

        setBeforeProducer(exchange -> {
            // convert json into ClientEntity
            Message in = exchange.getIn();
            ignoreResponseHeaders(in);
            final ODataBinder binder = odataClient.getBinder();
            final ClientODataDeserializer deserializer = odataClient.getDeserializer(ContentType.APPLICATION_JSON);
            ClientEntity oDataEntity = binder.getODataEntity(deserializer.toEntity(in.getBody(InputStream.class)));
            in.setBody(oDataEntity);
        });
    }

}
