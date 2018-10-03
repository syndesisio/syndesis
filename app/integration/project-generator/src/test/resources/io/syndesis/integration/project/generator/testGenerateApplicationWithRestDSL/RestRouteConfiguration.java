package io.syndesis.example;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestRouteConfiguration {

    String basePath;

    public RestRouteConfiguration(final @Value("${api-basePath}") String basePath) {
        this.basePath = basePath;
    }

    @Bean
    public RouteBuilder specificationRoute() {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                restConfiguration()
                    .contextPath(basePath)
                    .apiContextPath(".api-doc")
                    .apiContextRouteId("doc-api")
                    .component("servlet");
            }
        };
    }

    @Bean
    public RouteBuilder restRoute() {
        return new RestRoute();
    }
}
