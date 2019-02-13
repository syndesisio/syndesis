import { boolean, select, text } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { FormTextAreaComponent } from '../src/widgets/FormTextAreaComponent';

const stories = storiesOf('Textarea', module);

stories.add('Basic Textarea', () => {
  return (
    <FormTextAreaComponent
      field={{
        name: 'brokerCertificate',
        value: text('Value', ''),
        onChange: () => {},
      }}
      form={{ isSubmitting: false }}
      property={{
        disabled: boolean('Disabled', false),
        displayName: text('Label', 'Broker Certificate'),
        description: text('Description', 'AMQ Broker X.509 PEM Certificate'),
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
