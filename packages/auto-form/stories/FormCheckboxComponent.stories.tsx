import { boolean, select } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { FormCheckboxComponent } from '../src/widgets/FormCheckboxComponent';

const stories = storiesOf('Checkbox', module);

export const fieldObj = {
  name: 'showAll',
  displayName: 'Log Everything',
  description: 'whether or not to log everything (very verbose).',
  value: true,
  disabled: false,
  onChange: () => {},
  validationState: '',
};

stories.add('FormCheckboxComponent', () => {
  return (
    <FormCheckboxComponent
      field={{
        name: fieldObj.name,
        value: boolean('Checked', fieldObj.value),
        onChange: fieldObj.onChange,
      }}
      form={{ isSubmitting: false }}
      property={{
        disabled: boolean('Disabled', fieldObj.disabled),
        displayName: fieldObj.displayName,
        description: fieldObj.description,
      }}
      validationState={select('Validation State', [
        fieldObj.validationState,
        'success',
        'warning',
        'error',
      ])}
    />
  );
});
