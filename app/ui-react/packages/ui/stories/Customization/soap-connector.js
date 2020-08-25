export default {
  name: 'HelloWorldImplService',
  properties: {
    address: {
      defaultValue: 'http://localhost:9191/HelloWorld',
      description:
        'SOAP Endpoint address from WSDL SOAP Binding or user specified address.',
      displayName: 'Address',
    },
    addTimestamp: {
      defaultValue: false,
      description: 'Add a Timestamp to WS-Security header.',
      displayName: 'Timestamp',
    },
    addUsernameTokenCreated: {
      defaultValue: false,
      description:
        'Add Created timestamp element to WS-Security Username Token header.',
      displayName: 'Username Token Created',
    },
    addUsernameTokenNonce: {
      defaultValue: false,
      description: 'Add Nonce element to WS-Security Username Token header.',
      displayName: 'Username Token Nonce',
    },
    authenticationType: {
      defaultValue: 'none',
      description:
        'Authentication Type to use to invoke WSDL endpoint, one of [None|Basic|WS-Security Username Token].',
      displayName: 'Authentication Type',
      enum: [
        {
          label: 'HTTP Basic Authentication',
          value: 'basic',
        },
        {
          label: 'None',
          value: 'none',
        },
        {
          label: 'WS-Security Username Token',
          value: 'ws-security-ut',
        },
      ],
    },
    authorizationEndpoint: {
      defaultValue: 'auth-endpoint',
    },
    passwordType: {
      defaultValue: 'PasswordNone',
      displayName: 'Password Type',
      enum: [
        {
          label: 'None',
          value: 'PasswordNone',
        },
        {
          label: 'Text',
          value: 'PasswordText',
        },
        {
          label: 'Digest',
          value: 'PasswordDigest',
        },
      ],
    },
    tokenEndpoint: {
      defaultValue: '',
      displayName: 'Token Endpoint',
    },
  },
  configuredProperties: {
    componentName: 'connector-soap',
    serviceName: '{http://camel.apache.org/cxf/wsrm}HelloWorldService',
    portName: 'HelloWorldPort',
    address: 'http://localhost:9191/HelloWorld',
    services: '["{http://camel.apache.org/cxf/wsrm}HelloWorldService"]',
    ports:
      '{"{http://camel.apache.org/cxf/wsrm}HelloWorldService": ["HelloWorldPort"]}',
  },
  description:
    'Web Services Connector for service {http://camel.apache.org/cxf/wsrm}HelloWorldImplService',
};
