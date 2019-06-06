import { action } from '@storybook/addon-actions';
import { boolean, text } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { ExtensionImportCard } from '../../src';

export const extensionImportStory = 'import one jar';
const stories = storiesOf(
  'Customization/Extensions/Component/ExtensionImportCard',
  module
);
const dndInstructions =
  "Drag 'n' drop a file here, or <strong>click</strong> to select a file using a file chooser dialog.";

const handleFiles = (files: File[]) => {
  files.forEach(file => {
    action('Process file ' + file.name + '\n');
  });
};
const helpMessage = 'Accepted file type: .jar';
const noFileSelectedMessage = 'no file selected';
const selectedFileLabel = 'Selected file:';
const uploadFailedMessage = (fileName: string) =>
  '<span>File <strong>' + fileName + '</strong> could not be uploaded</span>';
const uploadSuccessMessage = 'Successfully uploaded.';

const storyNotes =
  '- Verify DnD drop area instructions are "' +
  dndInstructions +
  '"\n' +
  '- Verify the DnD drop area selected file label is "' +
  selectedFileLabel +
  '"\n' +
  '- Verify the DnD drop area no file selected message is "' +
  noFileSelectedMessage +
  '"\n' +
  '- Verify the DnD drop area help message is "' +
  helpMessage +
  '"\n' +
  '- Drag a .jar file into the drop area and verify the file name is displayed\n' +
  '- After the drag of the .jar file verify the success message is "' +
  uploadSuccessMessage +
  '"\n' +
  '- Drag a non-.jar file into the drop area and verify a toast with the file name is displayed.\n' +
  '- Verify DnD of multiple files does not work.\n' +
  '- In the **Knobs** tab, make sure disabling the dropzone works.\n' +
  '- In the **Knobs** tab, make sure if neither a fail message or success message is printed.\n' +
  '- In the **Knobs** tab, make sure adding an alert message displays an alert (and is removed when message is deleted).\n' +
  '- In the **Knobs** tab, if only a fail message is entered, make sure the error icon is used.\n' +
  '- In the **Knobs** tab, if a success message is entered, make sure the OK icon is used.\n';

stories.add(
  extensionImportStory,
  () => (
    <ExtensionImportCard
      dndDisabled={boolean('Disabled', false)}
      i18nAlertMessage={text('Alert Message', undefined)}
      i18nDndHelpMessage={helpMessage}
      i18nDndInstructions={dndInstructions}
      i18nDndNoFileSelectedMessage={noFileSelectedMessage}
      i18nDndSelectedFileLabel={selectedFileLabel}
      i18nDndUploadFailedMessage={text('Failed Message', undefined)}
      i18nDndUploadSuccessMessage={text(
        'Success Message',
        'Successfully uploaded'
      )}
      onDndUploadAccepted={handleFiles}
      onDndUploadRejected={uploadFailedMessage}
    />
  ),
  { notes: storyNotes }
);
