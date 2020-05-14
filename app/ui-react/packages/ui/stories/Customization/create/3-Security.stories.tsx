// tslint:disable:object-literal-sort-keys
import { action } from '@storybook/addon-actions';
import { boolean } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { ApiConnectorCreatorLayout } from '../../../src';
import {
  ApiClientConnectorCreateSecurity,
  ApiConnectorCreatorBreadSteps,
  ApiConnectorCreatorFooter,
  ApiConnectorCreatorToggleList,
} from '../../../src/Customization/apiClientConnectors/create';

const stories = storiesOf(
  'Customization/ApiClientConnector/CreateApiConnector/3 - Select Security',
  module
);

const soapSpec = {
  name: 'HelloWorldImplService',
  properties: {
    addTimestamp: {
      order: 6,
      componentProperty: true,
      description: 'Add a Timestamp to WS-Security header.',
      displayName: 'Timestamp',
      group: 'security',
      javaType: 'java.lang.Boolean',
      kind: 'property',
      label: 'common,security',
      relation: [
        {
          action: 'ENABLE',
          when: [
            {
              id: 'authenticationType',
              value: 'ws-security-ut',
            },
          ],
        },
      ],
      required: false,
      secret: false,
      type: 'boolean',
    },
    addUsernameTokenCreated: {
      order: 8,
      componentProperty: true,
      description:
        'Add Created timestamp element to WS-Security Username Token header.',
      displayName: 'Username Token Created',
      group: 'security',
      javaType: 'java.lang.Boolean',
      kind: 'property',
      label: 'common,security',
      relation: [
        {
          action: 'ENABLE',
          when: [
            {
              id: 'passwordType',
              value: 'PasswordText',
            },
          ],
        },
      ],
      required: false,
      secret: false,
      type: 'boolean',
    },
    addUsernameTokenNonce: {
      order: 7,
      componentProperty: true,
      description: 'Add Nonce element to WS-Security Username Token header.',
      displayName: 'Username Token Nonce',
      group: 'security',
      javaType: 'java.lang.Boolean',
      kind: 'property',
      label: 'common,security',
      relation: [
        {
          action: 'ENABLE',
          when: [
            {
              id: 'passwordType',
              value: 'PasswordText',
            },
          ],
        },
      ],
      required: false,
      secret: false,
      type: 'boolean',
    },
    address: {
      order: 1,
      componentProperty: true,
      defaultValue: 'http://localhost:9191/HelloWorld',
      description:
        'SOAP Endpoint address from WSDL SOAP Binding or user specified address.',
      displayName: 'Address',
      javaType: 'java.lang.String',
      required: true,
      type: 'string',
    },
    authenticationType: {
      order: 2,
      componentProperty: true,
      description:
        'Authentication Type to use to invoke WSDL endpoint, one of [None|Basic|WS-Security Username Token].',
      displayName: 'Authentication Type',
      group: 'security',
      javaType: 'java.lang.String',
      kind: 'property',
      label: 'common,security',
      required: true,
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
    password: {
      order: 5,
      componentProperty: true,
      description: 'Authentication password.',
      displayName: 'Password',
      group: 'security',
      javaType: 'java.lang.String',
      kind: 'property',
      label: 'common,security',
      relation: [
        {
          action: 'DISABLE',
          when: [
            {
              id: 'passwordType',
              value: 'none',
            },
          ],
        },
      ],
      required: false,
      secret: true,
      type: 'string',
    },
    passwordType: {
      order: 3,
      componentProperty: true,
      description: 'WS-Security Password Type.',
      displayName: 'Password Type',
      group: 'security',
      javaType: 'java.lang.String',
      kind: 'property',
      label: 'common,security',
      relation: [
        {
          action: 'ENABLE',
          when: [
            {
              id: 'authenticationType',
              value: 'ws-security-ut',
            },
          ],
        },
      ],
      required: false,
      secret: false,
      type: 'string',
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
    portName: {
      componentProperty: true,
      description: 'Target Port in WSDL.',
      displayName: 'Port Name',
      group: 'common',
      javaType: 'java.lang.String',
      kind: 'property',
      label: 'common',
      required: true,
      type: 'hidden',
    },
    serviceName: {
      componentProperty: true,
      description: 'Target service in WSDL.',
      displayName: 'Service Name',
      group: 'common',
      javaType: 'java.lang.String',
      kind: 'property',
      label: 'common',
      required: true,
      type: 'hidden',
    },
    soapVersion: {
      componentProperty: true,
      description: 'SOAP Binding version in WSDL.',
      displayName: 'SOAP Version',
      group: 'common',
      javaType: 'java.lang.Double',
      kind: 'property',
      label: 'common',
      required: true,
      type: 'hidden',
    },
    specification: {
      componentProperty: true,
      description: 'WSDL document defining the service.',
      displayName: 'WSDL Document',
      group: 'common',
      javaType: 'java.lang.String',
      kind: 'property',
      label: 'common',
      required: true,
      type: 'hidden',
    },
    username: {
      order: 4,
      componentProperty: true,
      description: 'Authentication username.',
      displayName: 'Username',
      group: 'security',
      javaType: 'java.lang.String',
      kind: 'property',
      label: 'common,security',
      required: false,
      secret: false,
      type: 'string',
    },
  },
  configuredProperties: {
    specification:
      '<?xml version="1.0" encoding="UTF-8"?><wsdl:definitions xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/" xmlns:ns1="http://schemas.xmlsoap.org/soap/http" xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/" xmlns:tns="http://camel.apache.org/cxf/wsrm" xmlns:xsd="http://www.w3.org/2001/XMLSchema" name="HelloWorldImplService" targetNamespace="http://camel.apache.org/cxf/wsrm"><wsdl:types><xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns="http://camel.apache.org/cxf/wsrm" attributeFormDefault="unqualified" elementFormDefault="unqualified" targetNamespace="http://camel.apache.org/cxf/wsrm"><xs:complexType name="sayHi"><xs:sequence><xs:element minOccurs="0" name="arg0" type="xs:string"/></xs:sequence></xs:complexType><xs:complexType name="sayHiResponse"><xs:sequence><xs:element minOccurs="0" name="return" type="xs:string"/></xs:sequence></xs:complexType><xs:element name="sayHi" nillable="true" type="sayHi"/><xs:element name="sayHiResponse" nillable="true" type="sayHiResponse"/></xs:schema></wsdl:types><wsdl:message name="sayHi"><wsdl:part element="tns:sayHi" name="parameters"/></wsdl:message><wsdl:message name="sayHiResponse"><wsdl:part element="tns:sayHiResponse" name="parameters"/></wsdl:message><wsdl:portType name="HelloWorld"><wsdl:operation name="sayHi"><wsdl:input message="tns:sayHi" name="sayHi"/><wsdl:output message="tns:sayHiResponse" name="sayHiResponse"/></wsdl:operation></wsdl:portType><wsdl:binding name="HelloWorldServiceSoapBinding" type="tns:HelloWorld"><soap:binding style="document" transport="http://schemas.xmlsoap.org/soap/http"/><wsdl:operation name="sayHi"><soap:operation soapAction="" style="document"/><wsdl:input name="sayHi"><soap:body use="literal"/></wsdl:input><wsdl:output name="sayHiResponse"><soap:body use="literal"/></wsdl:output></wsdl:operation></wsdl:binding><wsdl:service name="HelloWorldService"><wsdl:port binding="tns:HelloWorldServiceSoapBinding" name="HelloWorldPort"><soap:address location="http://localhost:9191/HelloWorld"/><wswa:UsingAddressing xmlns:wswa="http://www.w3.org/2005/02/addressing/wsdl"/></wsdl:port></wsdl:service></wsdl:definitions>',
    componentName: 'connector-soap',
    serviceName: '{http://camel.apache.org/cxf/wsrm}HelloWorldService',
    portName: 'HelloWorldPort',
    address: 'http://localhost:9191/HelloWorld',
    services: '["{http://camel.apache.org/cxf/wsrm}HelloWorldService"]',
    ports:
      '{"{http://camel.apache.org/cxf/wsrm}HelloWorldService": ["HelloWorldPort"]}',
  },
  actionsSummary: {
    actionCountByTags: {
      sayHi: 1,
    },
    totalActions: 1,
  },
  description:
    'Web Services Connector for service {http://camel.apache.org/cxf/wsrm}HelloWorldImplService',
  icon:
    'data:image/svg+xml,%3Csvg%20xmlns%3Asvg%3D%22http%3A%2F%2Fwww%2Ew3%2Eorg%2F2000%2Fsvg%22%20xmlns%3D%22http%3A%2F%2Fwww%2Ew3%2Eorg%2F2000%2Fsvg%22%20width%3D%22400%22%20height%3D%22400%22%3E%3Ccircle%20cx%3D%22200%22%20cy%3D%22200%22%20r%3D%22200%22%20style%3D%22fill%3A%23fff%22%2F%3E%3Cpath%20d%3D%22m%20258%2E608%2C280%20%2D11%2E536%2C0%200%2C%2D80%2E528%20%2D95%2E648%2C0%200%2C80%2E528%20%2D11%2E424%2C0%200%2C%2D163%2E744%2011%2E424%2C0%200%2C72%2E688%2095%2E648%2C0%200%2C%2D72%2E688%2011%2E536%2C0%200%2C163%2E744%20z%22%20style%3D%22fill%3A%23fff%22%2F%3E%3C%2Fsvg%3E',
};

stories.add('Specify Security (SOAP)', () => {
  return (
    <ApiConnectorCreatorLayout
      content={
        <ApiClientConnectorCreateSecurity
          authenticationTypes={[]}
          authUrl={''}
          extractAuthType={(params?: string) => ''}
          handleChangeAuthUrl={action('handleChangeAuthUrl')}
          handleChangeSelectedType={action('handleChangeSelectedType')}
          handleChangeTokenUrl={action('handleChangeTokenUrl')}
          i18nAccessTokenUrl={'Access Token URL'}
          i18nAuthorizationUrl={
            'apiClientConnectors:create:security:authorizationUrl'
          }
          i18nDescription={'apiClientConnectors:create:security:description'}
          i18nNoSecurity={'apiClientConnectors:create:security:noSecurity'}
          i18nTitle={'Specify Security'}
          selectedType={'selectedType'}
          tokenUrl={'tokenUrl'}
        />
      }
      footer={
        <ApiConnectorCreatorFooter
          backHref={''}
          onNext={action('')}
          i18nBack={'Back'}
          i18nNext={'Next'}
          isNextLoading={boolean('isNextLoading', false)}
          isNextDisabled={boolean('isNextDisabled', false)}
        />
      }
      navigation={
        <ApiConnectorCreatorBreadSteps
          step={3}
          i18nDetails={'Review/Edit Connector Details'}
          i18nReview={'Imported Operations'}
          i18nSecurity={'Specify Security'}
          i18nSelectMethod={'Provide Document'}
        />
      }
      toggle={
        <ApiConnectorCreatorToggleList
          step={1}
          i18nDetails={'Review/Edit Connector Details'}
          i18nReview={'Imported Operations'}
          i18nSecurity={'Specify Security'}
          i18nSelectMethod={'Provide Document'}
        />
      }
    />
  );
});

stories.add('Specify Security (REST)', () => (
  <ApiConnectorCreatorLayout
    content={
      <ApiClientConnectorCreateSecurity
        authenticationTypes={[]}
        authUrl={''}
        extractAuthType={(params?: string) => ''}
        handleChangeAuthUrl={action('handleChangeAuthUrl')}
        handleChangeSelectedType={action('handleChangeSelectedType')}
        handleChangeTokenUrl={action('handleChangeTokenUrl')}
        i18nAccessTokenUrl={'Access Token URL'}
        i18nAuthorizationUrl={
          'apiClientConnectors:create:security:authorizationUrl'
        }
        i18nDescription={'apiClientConnectors:create:security:description'}
        i18nNoSecurity={'apiClientConnectors:create:security:noSecurity'}
        i18nTitle={'Specify Security'}
        selectedType={'selectedType'}
        tokenUrl={'tokenUrl'}
      />
    }
    footer={
      <ApiConnectorCreatorFooter
        backHref={''}
        onNext={action('')}
        i18nBack={'Back'}
        i18nNext={'Next'}
        isNextLoading={boolean('isNextLoading', false)}
        isNextDisabled={boolean('isNextDisabled', false)}
      />
    }
    navigation={
      <ApiConnectorCreatorBreadSteps
        step={3}
        i18nDetails={'Review/Edit Connector Details'}
        i18nReview={'Imported Operations'}
        i18nSecurity={'Specify Security'}
        i18nSelectMethod={'Provide Document'}
      />
    }
    toggle={
      <ApiConnectorCreatorToggleList
        step={1}
        i18nDetails={'Review/Edit Connector Details'}
        i18nReview={'Imported Operations'}
        i18nSecurity={'Specify Security'}
        i18nSelectMethod={'Provide Document'}
      />
    }
  />
));
