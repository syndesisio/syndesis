import { action } from '@storybook/addon-actions';
import { boolean } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import {
  ConnectionDetailsForm,
  IConnectionDetailsValidationResult,
} from '../../src';

const stories = storiesOf('Connection/ConnectionDetailsForm', module);
const cancelLabel = 'Cancel';
const editLabel = 'Edit';
const errorMessage = 'An error message';
const saveLabel = 'Save';
const successMessage = 'A success message';
const title = 'Connection Details Form';
const validateLabel = 'Validate';

const cancelEditingText = 'editing canceled';
const startEditingText = 'editing started';
const submitText = 'saving';
const validateText = 'validating';

const validationResults: IConnectionDetailsValidationResult[] = [
  { message: successMessage, type: 'success' },
  { message: errorMessage, type: 'error' },
];

const storyNotes =
  '- Verify title is "' +
  title +
  '"\n' +
  '- Verify there is a success message with the text: "' +
  successMessage +
  '"\n' +
  '- Verify there is an error message with the text: "' +
  errorMessage +
  '"\n' +
  '- Verify there is an edit button that is enabled and has the text of "' +
  editLabel +
  '"\n' +
  '- Verify clicking the edit button prints "' +
  startEditingText +
  '" in the action log' +
  '"\n' +
  '- Verify clicking on the **isEditing** knob hides the edit button and shows the validate, cancel, and save buttons' +
  '"\n' +
  '- Verify the validate button is enabled and has the text of "' +
  validateLabel +
  '"\n' +
  '- Verify clicking the validate button prints "' +
  validateText +
  '" in the action log' +
  '"\n' +
  '- Verify the cancel button is enabled and has the text of "' +
  cancelLabel +
  '"\n' +
  '- Verify clicking the cancel button prints "' +
  cancelEditingText +
  '" in the action log' +
  '"\n' +
  '- Verify the save button is enabled and has the text of "' +
  saveLabel +
  '"\n' +
  '- Verify clicking the save button prints "' +
  submitText +
  '" in the action log' +
  '"\n' +
  '- Verify clicking on the **isWorking** knob shows a spinner on the validate button and disables the cancel and save buttons' +
  '"\n' +
  '- Verify clicking off the **isEditing** knob hides the validate, cancel, and save buttons and shows the edit button.';

stories.add(
  'render',
  () => (
    <ConnectionDetailsForm
      handleSubmit={action(submitText)}
      i18nCancelLabel={cancelLabel}
      i18nEditLabel={editLabel}
      i18nSaveLabel={saveLabel}
      i18nTitle={title}
      i18nValidateLabel={validateLabel}
      isEditing={boolean('isEditing', false)}
      isValid={boolean('isValid', true)}
      isWorking={boolean('isWorking', false)}
      onCancelEditing={action(cancelEditingText)}
      onStartEditing={action(startEditingText)}
      onValidate={action(validateText)}
      validationResults={validationResults}
    />
  ),
  { notes: storyNotes }
);
