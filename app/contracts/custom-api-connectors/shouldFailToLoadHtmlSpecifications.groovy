package contracts.api.connectors

import org.springframework.cloud.contract.spec.Contract

Contract.make {

    description('Should fail to parse non-valid Swagger specifications, i.e. HTML files')

    request {
        method 'POST'
        url '/api/v1/connectors/custom/info'
        headers { contentType(applicationJson()) }
        body(
                connectorTemplateId: 'swagger-connector-template',
                configuredProperties: [
                    specification: 'http://petstore.swagger.io/index.html'
                ])
    }

    response {
        status 200
        headers { contentType(applicationJson()) }
        body(
                errors: [
                    [
                        error: 'error',
                        message: 'Unable to resolve Swagger specification from: http://petstore.swagger.io/index.html'
                    ]
                ],
                warnings: [],
                properties: [])
    }
}
