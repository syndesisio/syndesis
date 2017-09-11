package io.syndesis.verifier.v1;

import java.util.Map;
import java.util.Optional;

import org.apache.camel.Endpoint;
import org.apache.camel.component.extension.MetaDataExtension;
import org.apache.camel.component.extension.metadata.MetaDataBuilder;
import org.apache.camel.impl.DefaultComponent;

public class PetstoreComponent extends DefaultComponent {

    public PetstoreComponent(final Object payload) {
        registerExtension((MetaDataExtension) parameters -> Optional
            .of(MetaDataBuilder.on(getCamelContext()).withPayload(payload).build()));
    }

    @Override
    protected Endpoint createEndpoint(final String uri, final String remaining, final Map<String, Object> parameters)
        throws Exception {
        throw new UnsupportedOperationException();
    }
}