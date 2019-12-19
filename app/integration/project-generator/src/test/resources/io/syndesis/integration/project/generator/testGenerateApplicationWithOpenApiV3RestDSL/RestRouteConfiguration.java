package io.syndesis.example;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestRouteConfiguration {

    @Bean
    public RouteBuilder specificationRoute() {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                restConfiguration()
                    .contextPath("/")
                    .component("servlet")
                    .endpointProperty("headerFilterStrategy", "syndesisHeaderStrategy");

                rest()
                    .get("/openapi.json")
                    .description("Returns the OpenAPI specification for this service")
                    .route()
                    .setHeader(Exchange.CONTENT_TYPE, constant("application/vnd.oai.openapi+json"))
                    .setBody(constant("resource:classpath:openapi.json"));
            }
        };
    }

    @Bean
    public RouteBuilder restRoute() {
        return new RestRoute();
    }
}
