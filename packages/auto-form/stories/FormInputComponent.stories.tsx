import { boolean, number, select, text } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { FormInputComponent } from '../src/widgets/FormInputComponent';

const stories = storiesOf('TextInput', module);

stories.add('Basic Text Input', () => {
  return (
    <FormInputComponent
      type="text"
      field={{
        name: 'brokerCertificate',
        value: text('Value', ''),
        onChange: () => {},
      }}
      form={{
        isSubmitting: false,
      }}
      property={{
        disabled: boolean('Disabled', false),
        displayName: text('Display Name', 'Broker URL'),
        placeholder: text(
          'Placeholder',
          'for example, failover://ssl://{BROKER-HOST}:{BROKER-PORT}'
        ),
        description: text('Description', 'Enter the fully qualified URL'),
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

stories.add('Password Input', () => {
  return (
    <FormInputComponent
      type="password"
      field={{
        name: 'passwordInput',
        value: text('Value', 'secret'),
        onChange: () => {},
      }}
      form={{
        isSubmitting: false,
      }}
      property={{
        disabled: boolean('Disabled', false),
        displayName: text('Display Name', 'Password'),
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

stories.add('Number Input', () => {
  return (
    <FormInputComponent
      type="number"
      field={{
        name: 'numberInput',
        defaultValue: 2,
        min: number('Min', 0),
        max: number('Max', 10),
        onChange: () => {},
      }}
      form={{
        isSubmitting: false,
      }}
      property={{
        disabled: boolean('Disabled', false),
        displayName: text('Display Name', 'Client Number'),
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
