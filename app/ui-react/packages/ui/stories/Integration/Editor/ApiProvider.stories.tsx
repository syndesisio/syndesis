import { action } from '@storybook/addon-actions';
import { boolean, text } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import {
  ApiProviderSelectMethod,
  ApiProviderReviewActions,
  ApiProviderReviewOperations,
} from '../../../src';

const fileExtensions = '.json, .yaml and .yml';
const helpMessage = 'Accepted file type: .json, .yaml and .yml';
const instructions =
  "Drag 'n' drop a file here, or <strong>click</strong> to select a file using a file chooser dialog.";
const noFileSelectedMessage = 'no file selected';
const handleFiles = (files: File[]) => {
  files.forEach(file => {
    action('Process file ' + file.name + '\n');
    logUploadSucceedMessage();
  });
};
const logUploadFailedMessage = action('upload failed message handler');
const logUploadSucceedMessage = action('upload succeeded message handler');

const selectedFileLabel = 'Selected file:';
const handleSubmit = (e: Event) => {
  action('Handle form here');
};
const uploadFailedMessage = (fileName: string) => {
  logUploadFailedMessage();
  return (
    '<span>File <strong>' + fileName + '</strong> could not be uploaded</span>'
  );
};

const stories = storiesOf('Integration/Editor/ApiProvider', module);

stories
  .add('Select Method', () => (
    <ApiProviderSelectMethod
      i18nMethodFromFile={'Upload an OpenAPI file'}
      i18nMethodFromScratch={'Create from scratch'}
      i18nMethodFromUrl={'Use a URL'}
      i18nUrlNote={
        '* Note: After uploading this document, Syndesis does not automatically obtain any updates to it.'
      }
      disableDropzone={boolean('Disabled', false)}
      fileExtensions={fileExtensions}
      handleSubmit={handleSubmit}
      i18nHelpMessage={helpMessage}
      i18nInstructions={instructions}
      i18nNoFileSelectedMessage={noFileSelectedMessage}
      i18nSelectedFileLabel={selectedFileLabel}
      i18nUploadFailedMessage={text('Fail Message', 'Upload failed')}
      i18nUploadSuccessMessage={text('Success Message', undefined)}
      onUploadAccepted={handleFiles}
      onUploadRejected={uploadFailedMessage}
    />
  ))
  .add('Review Actions', () => (
    <ApiProviderReviewActions
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
  ))
  .add('Review Operations', () => <ApiProviderReviewOperations />);
