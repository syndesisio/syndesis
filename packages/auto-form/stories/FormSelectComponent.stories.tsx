import { boolean, select, object, text } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { FormSelectComponent } from '../src/widgets/FormSelectComponent';

const stories = storiesOf('Select', module);

stories.add('Basic Select', () => {
  return (
    <FormSelectComponent
      field={{
        name: 'skipCertificateCheck',
        onChange: () => {
          console.log('you changed the select value');
        },
      }}
      form={{ isSubmitting: false }}
      property={{
        disabled: boolean('Disabled', false),
        displayName: text('Label', 'Check Certificates'),
        description: text('Description', ''),
        enum: object('Values', [
          {
            label: 'Disable',
            value: 'true',
          },
          {
            label: 'Enable',
            value: 'false',
          },
        ]),
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
