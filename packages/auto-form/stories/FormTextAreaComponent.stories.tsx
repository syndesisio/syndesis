import { boolean, select, text } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { FormTextAreaComponent } from '../src/widgets/FormTextAreaComponent';

const stories = storiesOf('Textarea', module);

export const fieldObj = {
  name: 'brokerCertificate',
  displayName: 'Broker Certificate',
  description: 'AMQ Broker X.509 PEM Certificate',
  value: '',
  disabled: false,
  onChange: () => {},
  validationState: '',
};

stories.add('FormTextAreaComponent', () => {
  return (
    <FormTextAreaComponent
      field={{
        name: fieldObj.name,
        value: text('Value', fieldObj.value),
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
