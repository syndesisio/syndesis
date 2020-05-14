// tslint:disable:object-literal-sort-keys
import { action } from '@storybook/addon-actions';
import { boolean } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import {
  ApiConnectorCreateService,
  ApiConnectorCreatorLayout,
} from '../../../src';
import {
  ApiConnectorCreatorBreadSteps,
  ApiConnectorCreatorToggleList,
} from '../../../src/Customization/apiClientConnectors/create';
import { OpenApiSelectMethod } from '../../../src/Shared';

const stories = storiesOf(
  'Customization/ApiClientConnector/CreateApiConnector/1 - Provide Document',
  module
);

const soapUpload = {
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

stories.add('Provide Document', () => {
  let showSoapConfig = false;

  return (
    <>
      <ApiConnectorCreatorLayout
        content={
          <OpenApiSelectMethod
            disableDropzone={boolean('disableDropzone', false)}
            fileExtensions={'.json,.yaml,.yml,.wsdl'}
            i18nBtnNext={'Next'}
            i18nHelpMessage={
              'Accepted file type: .json, .yaml, .yml, and .wsdl'
            }
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
            onNext={() => (showSoapConfig = true)}
            allowFromScratch={boolean('allowFromScratch', false)}
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
      {/* Where users can specify a SOAP service and port if connector is WSDL file */}
      {showSoapConfig && (
        <ApiConnectorCreateService
          handleNext={action('onServiceConfigured')}
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
      )}
    </>
  );
});
