package io.syndesis.connector.amqp;

import java.util.HashMap;
import java.util.List;

import io.syndesis.verifier.api.Verifier;
import io.syndesis.verifier.api.VerifierResponse;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author dhirajsb
 */
public class AMQPVerifierTest {

    @Test
    public void testAMQPVerifier() throws Exception {
        CamelContext context = new DefaultCamelContext();
        context.start();
        AMQPVerifier amqpVerifier = new AMQPVerifier();
        final HashMap<String, Object> params = new HashMap<>();
        params.put("connectionUri", "amqps://messaging-maas-dbokde.6a63.fuse-ignite.openshiftapps.com:443");
        params.put("skipCertificateCheck", "true");
        params.put("username", "admin");
        params.put("password", "admin");
        final List<VerifierResponse> result = amqpVerifier.verify(context, "amqp-publish", params);
        assertThat(result.get(0).getStatus()).isEqualTo(Verifier.Status.OK);
    }
}