package contracts.api.connectors

import org.springframework.cloud.contract.spec.Contract

Contract.make {

    description('Provides information on the provided Swagger specification')

    request {
        method 'POST'
        url '/api/v1/connectors/custom/info'
        headers { contentType(applicationJson()) }
        body(
                connectorTemplateId: 'swagger-connector-template',
                configuredProperties: [
                    specification: 'http://petstore.swagger.io/v2/swagger.json'
                ])
    }

    response {
        status 200
        headers { contentType(applicationJson()) }
        body(
                actionsSummary: [
                    actionCountByTags: [
                        store: $(anyNumber()),
                        user: $(anyNumber()),
                        pet: $(anyNumber())
                    ],
                    totalActions: $(anyNumber())
                ])
    }
}
