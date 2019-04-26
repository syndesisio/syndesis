import { boolean, withKnobs } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { InlineTextEdit } from '../../src';

const stories = storiesOf('Shared/InlineTextEdit', module);
stories.addDecorator(withKnobs);

const fooErrorMsg = 'Value cannot be foo';
const requiredErrorMsg = 'Value cannot be empty';
const placeholder = 'Placeholder...';
const value = 'This is a value';

const handleValidate = (newValue: string): Promise<string | true> => {
  if (!newValue || newValue.length === 0) {
    return Promise.resolve(requiredErrorMsg);
  }

  if (newValue === 'foo') {
    return Promise.resolve(fooErrorMsg);
  }

  return Promise.resolve(true) as Promise<true>;
};

const handleValueChanged = (newValue: string) => {
  return Promise.resolve(true); // this is where value is persisted
};

const textFieldNotes =
  '- Verify when first rendered the value is readonly and has an pencil edit icon next to the value.\n' +
  '- Verify clicking the edit icon opens up the inline text field editor with the correct value.\n' +
  '- Verify deleting all text displays a placeholder of "' +
  placeholder +
  '", displays an error message of "' +
  requiredErrorMsg +
  '", and disables the confirm button.\n' +
  '- Verify entering "foo" as a value displays an error message of "' +
  fooErrorMsg +
  '" and the confirm button is disabled.\n' +
  '- Verify hitting the close button closes the editor and restores the original value.\n' +
  '- Verify entering a value other than "foo" enables the confirm button.\n' +
  '- Verify clicking the confirm button sets the new value on the readonly component.\n' +
  '- Verify unchecking the **allowEditing** knob removes the edit icon.\n';

const textAreaNotes =
  '- Verify when first rendered the value is readonly and has an pencil edit icon next to the value.\n' +
  '- Verify clicking the edit icon opens up the inline textarea editor with the correct value.\n' +
  '- Verify deleting all text displays a placeholder of "' +
  placeholder +
  '", displays an error message of "' +
  requiredErrorMsg +
  '", and disables the confirm button.\n' +
  '- Verify entering "foo" as a value displays an error message of "' +
  fooErrorMsg +
  '" and the confirm button is disabled.\n' +
  '- Verify hitting the close button closes the editor and restores the original value.\n' +
  '- Verify entering a value other than "foo" enables the confirm button.\n' +
  '- Verify clicking the confirm button sets the new value on the readonly component.\n' +
  '- Verify unchecking the **allowEditing** knob removes the edit icon.\n';

stories.add(
  'render text field',
  () => (
    <InlineTextEdit
      value={value}
      allowEditing={boolean('allowEditing', true)}
      i18nPlaceholder={placeholder}
      isTextArea={false}
      onChange={handleValueChanged}
      onValidate={handleValidate}
    />
  ),
  { notes: textFieldNotes }
);

stories.add(
  'render textarea',
  () => (
    <InlineTextEdit
      value={'This is a value'}
      allowEditing={boolean('allowEditing', true)}
      i18nPlaceholder={'Placeholder...'}
      isTextArea={true}
      onChange={handleValueChanged}
      onValidate={handleValidate}
    />
  ),
  { notes: textAreaNotes }
);
