import { action } from '@storybook/addon-actions';
import { boolean, text } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';

import {
  ApiConnectorCreatorSelectMethod,
  OpenApiReviewActions,
  SyndesisAlert,
  SyndesisAlertLevel,
} from '../../../src';

const fileExtensions = '.json, .yaml and .yml';
const helpMessage = 'Accepted file type: .json, .yaml and .yml';
const instructions =
  "Drag 'n' drop a file here, or <strong>click</strong> to select a file using a file chooser dialog.";
const noFileSelectedMessage = 'no file selected';

const selectedFileLabel = 'Selected file:';

const stories = storiesOf('Integration/Editor/ApiProvider', module);

stories
  .add('Select Method', () => (
    <ApiConnectorCreatorSelectMethod
      i18nBtnNext={'Next'}
      i18nMethodFromFile={'Upload an OpenAPI file'}
      i18nMethodFromScratch2x={'Create a new OpenAPI 2.x document'}
      i18nMethodFromScratch3x={'Create a new OpenAPI 3.x document'}
      i18nMethodFromUrl={'Use a URL'}
      i18nUrlNote={
        '* Note: After uploading this document, Syndesis does not automatically obtain any updates to it.'
      }
      disableDropzone={boolean('Disabled', false)}
      fileExtensions={fileExtensions}
      i18nHelpMessage={helpMessage}
      i18nInstructions={instructions}
      i18nNoFileSelectedMessage={noFileSelectedMessage}
      i18nSelectedFileLabel={selectedFileLabel}
      i18nUploadFailedMessage={text('Fail Message', 'Upload failed')}
      i18nUploadSuccessMessage={text('Success Message', undefined)}
      onNext={action('Click next')}
    />
  ))
  .add('Review Actions', () => (
    <OpenApiReviewActions
      alert={
        <SyndesisAlert
          level={SyndesisAlertLevel.WARN}
          message={'Some weird error has occurred'}
          detail={'this is the detail'}
          i18nTextCollapsed={'Show Details'}
          i18nTextExpanded={'Hide Details'}
        />
      }
      apiProviderDescription={'the description'}
      apiProviderName={'theName'}
      i18nApiDefinitionHeading={'API DEFINITION'}
      i18nDescriptionLabel={'Description'}
      i18nImportedHeading={'IMPORTED'}
      i18nNameLabel={'Name'}
      i18nOperationsHtmlMessage={'<strong>49</strong> operations'}
      i18nOperationTagHtmlMessages={[
        '- <strong>1</strong> tagged KIE Server Script :: Core',
        '- <strong>23</strong> tagged User tasks administration :: BPM',
        '- <strong>24</strong> tagged Queries - processes, nodes, variables and tasks :: BPM',
        '- <strong>1</strong> tagged Rules evaluation :: Core',
      ]}
    />
  ));
