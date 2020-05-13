import { action } from '@storybook/addon-actions';
import { boolean } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { ApiConnectorCreatorLayout } from '../../../src';
import {
  ApiConnectorCreatorBreadSteps,
  ApiConnectorCreatorToggleList,
} from '../../../src/Customization/apiClientConnectors/create';
import { OpenApiSelectMethod } from '../../../src/Shared';

const stories = storiesOf(
  'Customization/ApiClientConnector/Create/ApiConnectorCreator',
  module
);

stories.add('Select Method', () => (
  <ApiConnectorCreatorLayout
    content={
      <OpenApiSelectMethod
        disableDropzone={boolean('disableDropzone', false)}
        fileExtensions={'.json,.yaml,.yml'}
        i18nBtnNext={'Next'}
        i18nHelpMessage={'Accepted file type: .json, .yaml, and .yml'}
        i18nInstructions={
          'Drag and drop a file here, or <strong>click</strong> to select a file by using a file chooser dialog.'
        }
        i18nNoFileSelectedMessage={'No file selected'}
        i18nSelectedFileLabel={'Selected file:'}
        i18nUploadFailedMessage={' could not be uploaded'}
        i18nUploadSuccessMessage={'Process file '}
        i18nMethodFromFile={'Upload an OpenAPI file'}
        i18nMethodFromUrl={'Use a URL'}
        i18nUrlNote={
          '* Note: After uploading this document, updates to it are not automatically obtained.'
        }
        onNext={action('Next')}
        allowFromScratch={boolean('allowFromScratch', false)}
      />
    }
    navigation={
      <ApiConnectorCreatorBreadSteps
        step={1}
        i18nDetails={'Review/Edit Connector Details'}
        i18nReview={'Review Actions'}
        i18nSecurity={'Specify Security'}
        i18nSelectMethod={'Upload OpenAPI Document'}
      />
    }
    toggle={
      <ApiConnectorCreatorToggleList
        step={1}
        i18nDetails={'Review/Edit Connector Details'}
        i18nReview={'Review Actions'}
        i18nSecurity={'Specify Security'}
        i18nSelectMethod={'Upload OpenAPI Document'}
      />
    }
  />
));
