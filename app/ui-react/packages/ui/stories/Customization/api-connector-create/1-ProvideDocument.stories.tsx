import * as React from 'react';

import {
  ApiConnectorCreatorBreadSteps,
  ApiConnectorCreatorSelectMethod,
  ApiConnectorCreatorToggleList,
} from '../../../src/Customization/apiClientConnectors/create';
import {
  ApiConnectorCreatorLayout,
  ApiConnectorCreatorService,
} from '../../../src';

import { action } from '@storybook/addon-actions';
import { boolean } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';

const stories = storiesOf(
  'Customization/ApiClientConnector/CreateApiConnector/1 - Provide Document',
  module
);

const soapUpload = {
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

stories.add('Provide Document', () => {
  return (
    <ApiConnectorCreatorLayout
      content={
        <ApiConnectorCreatorSelectMethod
          disableDropzone={boolean('disableDropzone', false)}
          fileExtensions={'.json,.yaml,.yml,.wsdl'}
          i18nBtnNext={'Next'}
          i18nHelpMessage={'Accepted file type: .json, .yaml, .yml, and .wsdl'}
          i18nInstructions={
            'Drag and drop a file here, or <strong>click</strong> to select a file by using a file chooser dialog.'
          }
          i18nNoFileSelectedMessage={'No file selected'}
          i18nSelectedFileLabel={'Selected file:'}
          i18nUploadFailedMessage={' could not be uploaded'}
          i18nUploadSuccessMessage={'Process file '}
          i18nMethodFromFile={'Upload document'}
          i18nMethodFromUrl={'Use a URL'}
          i18nUrlNote={
            '* Note: After uploading this document, updates to it are not automatically obtained.'
          }
          onNext={action('onNext')}
        />
      }
      navigation={
        <ApiConnectorCreatorBreadSteps
          step={1}
          i18nConfiguration={'Additional Configuration'}
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

stories.add('Specify Service & Port (SOAP)', () => {
  return (
    <ApiConnectorCreatorLayout
      content={
        <ApiConnectorCreatorService
          handleNext={action('onServiceConfigured')}
          i18nBtnNext={'Next'}
          i18nPort={'Port'}
          i18nService={'Service'}
          i18nServicePortTitle={'Specify service and port'}
          portName={soapUpload!.configuredProperties!.portName}
          portsAvailable={JSON.parse(soapUpload!.configuredProperties!.ports)}
          serviceName={soapUpload!.configuredProperties!.serviceName}
          servicesAvailable={JSON.parse(
            soapUpload!.configuredProperties!.services
          )}
        />
      }
      navigation={
        <ApiConnectorCreatorBreadSteps
          step={1}
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
