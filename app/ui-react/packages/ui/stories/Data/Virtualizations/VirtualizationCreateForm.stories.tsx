import { action } from '@storybook/addon-actions';
import { boolean } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import {
  IVirtualizationCreateValidationResult,
  VirtualizationCreateForm,
} from '../../../src';

const stories = storiesOf('Data/Virtualizations/VirtualizationCreateForm', module);
const cancelLabel = 'Cancel';
const createLabel = 'Create';

const cancelText = 'cancel';
const createText = 'creating';
const errorMessage = 'An error message';
const successMessage = 'A success message';

const validationResults: IVirtualizationCreateValidationResult[] = [
  { message: successMessage, type: 'success' },
  { message: errorMessage, type: 'danger' },
];

const storyNotes =
  '- Verify the cancel button is enabled and has the text of "' +
  cancelLabel +
  '"\n' +
  '- Verify clicking the cancel button prints "' +
  cancelText +
  '" in the action log' +
  '"\n' +
  '- Verify the create button is enabled and has the text of "' +
  createLabel +
  '"\n' +
  '- Verify clicking the save button prints "' +
  createText +
  '" in the action log';

stories.add(
  'render',
  () => (
    <VirtualizationCreateForm
      handleSubmit={action(createText)}
      i18nCancelLabel={cancelLabel}
      i18nCreateLabel={createLabel}
      isWorking={boolean('isWorking', false)}
      validationResults={validationResults}
      onCancel={action(cancelText)}
    />
  ),
  { notes: storyNotes }
);
