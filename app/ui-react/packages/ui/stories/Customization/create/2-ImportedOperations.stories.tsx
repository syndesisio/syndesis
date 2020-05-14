// tslint:disable:object-literal-sort-keys
import { boolean } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { ApiConnectorCreatorLayout } from '../../../src';
import {
  ApiConnectorCreatorBreadSteps,
  ApiConnectorCreatorFooter,
  ApiConnectorCreatorToggleList,
} from '../../../src/Customization/apiClientConnectors/create';
import { OpenApiReviewActions } from '../../../src/Shared';

const stories = storiesOf(
  'Customization/ApiClientConnector/CreateApiConnector/2 - Imported Operations',
  module
);

const apiSummarySoap = {
  actionsSummary: {
    actionCountByTags: {
      sayHi: 1,
    },
    totalActions: 1,
  },
  configuredProperties: {
    componentName: 'connector-soap',
    serviceName: '{http://camel.apache.org/cxf/wsrm}HelloWorldService',
    portName: 'HelloWorldPort',
    address: 'http://localhost:9191/HelloWorld',
    services:
      '["{http://camel.apache.org/cxf/wsrm}HelloWorldService",' +
      ' "{http://camel.apache.org/cxf/wsrm}GoodbyeService"]',
    ports:
      '{' +
      '"{http://camel.apache.org/cxf/wsrm}HelloWorldService": ["HelloWorldPort1", "HelloWorldPort2"],' +
      '"{http://camel.apache.org/cxf/wsrm}GoodbyeService": ["GoodbyeWorldPort1", "GoodbyeWorldPort2"]' +
      '}',
  },
  description:
    'Web Services Connector for service {http://camel.apache.org/cxf/wsrm}HelloWorldImplService',
  name: 'HelloWorldImplService',
};

stories.add('Specify Service & Port (SOAP)', () => {
  return (
    <ApiConnectorCreatorLayout
      content={
        <OpenApiReviewActions
          apiProviderDescription={apiSummarySoap!.description}
          apiProviderName={apiSummarySoap!.name}
          errorMessages={[]}
          i18nApiDefinitionHeading={'API DEFINITION'}
          i18nDescriptionLabel={'Description'}
          i18nErrorsHeading={'ERRORS'}
          i18nImportedHeading={'IMPORTED'}
          i18nNameLabel={'Name'}
          i18nOperationsHtmlMessage={`<strong>${
            apiSummarySoap!.actionsSummary!.totalActions
          }</strong> operations`}
          i18nWarningsHeading={'WARNINGS'}
          warningMessages={[]}
        />
      }
      footer={
        <ApiConnectorCreatorFooter
          backHref={''}
          i18nBack={'Back'}
          i18nNext={'Next'}
          i18nReviewEdit={'Review/Edit'}
          isNextLoading={boolean('isNextLoading', false)}
          isNextDisabled={boolean('isNextDisabled', false)}
          nextHref={''}
          reviewEditHref={''}
        />
      }
      navigation={
        <ApiConnectorCreatorBreadSteps
          step={2}
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
