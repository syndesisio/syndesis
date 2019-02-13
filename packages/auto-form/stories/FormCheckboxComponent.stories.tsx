import { boolean, select, text } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { FormCheckboxComponent } from '../src/widgets/FormCheckboxComponent';

const stories = storiesOf('Checkbox', module);

stories.add('Basic Checkbox', () => {
  return (
    <FormCheckboxComponent
      field={{
        name: 'showAll',
        value: boolean('Checked', true),
        onChange: () => {},
      }}
      form={{ isSubmitting: false }}
      property={{
        disabled: boolean('Disabled', false),
        displayName: text('Label', 'Log Everything'),
        description: text(
          'Description',
          'whether or not to log everything (very verbose).'
        ),
      }}
      validationState={select('Validation State', [
        null,
        'success',
        'warning',
        'error',
      ])}
    />
  );
});
