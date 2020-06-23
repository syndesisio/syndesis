export default {
  name: 'GitHub',
  configuredProperties: {
    authenticationParameterName: 'api_key',
    authenticationParameterPlacement: 'query',
    authenticationType: 'apiKey:api_key',
    authorizationEndpoint: 'https://github.com/login/oauth/authorize',
    basePath: '/',
    componentName: 'connector-rest-swagger-http4',
    host: 'https://api.github.com',
    specification: '',
    tokenEndpoint: 'https://github.com/login/oauth/access_token',
  },
  properties: {
    authenticationParameterName: {},
    authenticationParameterPlacement: {},
    authenticationParameterValue: {},
    authenticationType: {},
    basePath: {},
    host: {},
    specification: {},
  },
  connectorGroup: {
    id: 'swagger-connector-template',
  },
  connectorGroupId: 'swagger-connector-template',
  description:
    'Powerful collaboration, code review, and code management for open source and private projects.',
  componentScheme: 'rest-openapi',
};
