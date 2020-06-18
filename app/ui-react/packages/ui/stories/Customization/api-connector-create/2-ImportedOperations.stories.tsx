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
    address: 'http://localhost:9191/HelloWorld',
    componentName: 'connector-soap',
    portName: 'HelloWorldPort',
    ports:
      '{' +
      '"{http://camel.apache.org/cxf/wsrm}HelloWorldService": ["HelloWorldPort1", "HelloWorldPort2"],' +
      '"{http://camel.apache.org/cxf/wsrm}GoodbyeService": ["GoodbyeWorldPort1", "GoodbyeWorldPort2"]' +
      '}',
    serviceName: '{http://camel.apache.org/cxf/wsrm}HelloWorldService',
    services:
      '["{http://camel.apache.org/cxf/wsrm}HelloWorldService",' +
      ' "{http://camel.apache.org/cxf/wsrm}GoodbyeService"]',
  },
  description:
    'Web Services Connector for service {http://camel.apache.org/cxf/wsrm}HelloWorldImplService',
  name: 'HelloWorldImplService',
};

stories.add('Review Actions', () => {
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
