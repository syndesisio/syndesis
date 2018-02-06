package io.syndesis.rest.metrics.prometheus;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import org.jboss.resteasy.client.jaxrs.internal.LocalResteasyProviderFactory;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJackson2Provider;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

public class HttpClient {

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModules(new Jdk8Module());

    public QueryResult queryPrometheus(HttpQuery query) {
        final Client client = createClient();
//        final WebTarget target = client.target(UriBuilder.fromPath(String.format("http://%s/api/v1/query", prometheusMetricsProviderImpl.getServiceName())).queryParam("query", "%7Bintegration=\"" + integrationId + "\",type=\"context\"%7D", integrationId));
        final WebTarget target = client.target(query.getUriBuilder());
        return target.request(MediaType.APPLICATION_JSON).get(QueryResult.class);
    }

    /* default */
    private Client createClient() {
        final ResteasyJackson2Provider resteasyJacksonProvider = new ResteasyJackson2Provider();
        resteasyJacksonProvider.setMapper(MAPPER);

        final ResteasyProviderFactory providerFactory = ResteasyProviderFactory.newInstance();
        providerFactory.register(resteasyJacksonProvider);
        final Configuration configuration = new LocalResteasyProviderFactory(providerFactory);

        return ClientBuilder.newClient(configuration);
    }
}
