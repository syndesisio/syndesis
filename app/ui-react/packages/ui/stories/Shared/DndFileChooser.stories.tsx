import { action } from '@storybook/addon-actions';
import { boolean, text } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { Container, DndFileChooser } from '../../src';

const stories = storiesOf('Shared/DndFileChooser', module);

const fileExtensions = '.jar';
const helpMessage = 'Accepted file type: .jar';
const instructions =
  "Drag 'n' drop a file here, or <strong>click</strong> to select a file using a file chooser dialog.";
const multiInstructions =
  "Drag 'n' drop one or more files here, or <strong>click</strong> to select files using a file chooser dialog.";
const multiNoFileSelectedMessage = 'no files selected';
const multiSelectedFileLabel = 'Selected files:';
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

const multiNotes =
  '- Verify the drop area has an etched outline\n' +
  '- Verify the instruction text is "' +
  multiInstructions +
  '"\n' +
  '- Verify the selected files label is "' +
  multiSelectedFileLabel +
  '"\n' +
  '- Verify text indicating that no files have been selected is "' +
  multiNoFileSelectedMessage +
  '"\n' +
  '- Verify the help message text is "' +
  helpMessage +
  '"\n' +
  '- Verify clicking in the drop area opens the system file chooser dialog.\n' +
  '- Verify the file chooser dialog allows multi-selection of *.jar files.\n' +
  '- Verify the file chooser dialog disallows selection of non-*.jar files.\n' +
  '- Verify DnD of *.jar files shows the files that were dropped.\n' +
  '- Verify the **Actions** prints the names of the files being uploaded.\n' +
  '- Verify DnD of *.jar files allows multiple and all are listed.\n' +
  '- Verify Dnd of non-*.jar files displays a toast notification for each disallowed file.\n' +
  '- In the **Knobs** tab, make sure disabling the dropzone works.\n' +
  '- In the **Knobs** tab, make sure if neither a fail message or success message is printed.\n' +
  '- In the **Knobs** tab, if only a fail message is entered, make sure the error icon is used.\n' +
  '- In the **Knobs** tab, if a success message is entered, make sure the OK icon is used.\n';

const singleNotes =
  '- Verify the drop area has an etched outline\n' +
  '- Verify the instruction text is "' +
  instructions +
  '"\n' +
  '- Verify the selected files label is "' +
  selectedFileLabel +
  '"\n' +
  '- Verify text indicating that no files have been selected is "' +
  noFileSelectedMessage +
  '"\n' +
  '- Verify the help message text is "' +
  helpMessage +
  '"\n' +
  '- Verify clicking in the drop area opens the system file chooser dialog.\n' +
  '- Verify the file chooser dialog does **NOT** allow multi-selection.\n' +
  '- Verify the file chooser dialog disallows selection of non-*.jar files.\n' +
  '- Verify DnD of *.jar file shows the file that was dropped (if multiple were dropped it will show first one).\n' +
  '- Verify the **Actions** tab prints the name of the file being uploaded.\n' +
  '- Verify DnD of a non-*.jar file displays a toast notification.\n' +
  '- In the **Knobs** tab, make sure disabling the dropzone works.\n' +
  '- In the **Knobs** tab, make sure if neither a fail message or success message is printed.\n' +
  '- In the **Knobs** tab, if only a fail message is entered, make sure the error icon is used.\n' +
  '- In the **Knobs** tab, if a success message is entered, make sure the OK icon is used.\n';

stories
  .add(
    'single file',
    () => (
      <Container style={{ margin: '50px' }}>
        <DndFileChooser
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
      </Container>
    ),
    { notes: singleNotes }
  )
  .add(
    'multiple files',
    () => (
      <Container style={{ margin: '50px' }}>
        <DndFileChooser
          allowMultiple={true}
          disableDropzone={boolean('Disabled', false)}
          fileExtensions={fileExtensions}
          i18nHelpMessage={helpMessage}
          i18nInstructions={multiInstructions}
          i18nNoFileSelectedMessage={multiNoFileSelectedMessage}
          i18nSelectedFileLabel={multiSelectedFileLabel}
          i18nUploadFailedMessage={text('Fail Message', undefined)}
          i18nUploadSuccessMessage={text(
            'Success Message',
            'Successfully uploaded'
          )}
          onUploadAccepted={handleFiles}
          onUploadRejected={uploadFailedMessage}
        />
      </Container>
    ),
    { notes: multiNotes }
  );
