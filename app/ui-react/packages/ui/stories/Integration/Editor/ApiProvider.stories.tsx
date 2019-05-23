import { action } from '@storybook/addon-actions';
import { boolean, text } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import {
  ApiProviderSelectMethod,
  ApiProviderReviewActions,
  ApiProviderReviewOperations,
  ApiProviderSetInfo,
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
const uploadFailedMessage = (fileName: string) => {
  logUploadFailedMessage();
  return (
    '<span>File <strong>' + fileName + '</strong> could not be uploaded</span>'
  );
};

const stories = storiesOf('Integration/Editor/ApiProvider', module);

stories
  .add('Select Method', () => (
    <>
      <ApiProviderSelectMethod
        i18nDescription={
          'Execute this integration when a client invokes an operation defined by this API.'
        }
        i18nMethodFromFile={'Upload an OpenAPI file'}
        i18nMethodFromScratch={'Create from scratch'}
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
        onUploadAccepted={handleFiles}
        onUploadRejected={uploadFailedMessage}
      />
    </>
  ))
  .add('Review Actions', () => (
    <>
      <ApiProviderReviewActions />
    </>
  ))
  .add('Set Information', () => (
    <>
      <ApiProviderSetInfo />
    </>
  ))
  .add('Review Operations', () => (
    <>
      <ApiProviderReviewOperations />
    </>
  ));
