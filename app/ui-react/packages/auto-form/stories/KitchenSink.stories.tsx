import { action } from '@storybook/addon-actions';
import { object, text } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { AutoForm, IFormDefinition } from '../src';
import { FormWrapper } from './FormWrapper';
import { StoryWrapper } from './StoryWrapper';

const stories = storiesOf('AutoForm General Examples', module);

const definition = {
  brokerCertificate: {
    componentProperty: true,
    deprecated: false,
    description: 'AMQ Broker X.509 PEM Certificate',
    displayName: 'Broker Certificate',
    group: 'security',
    javaType: 'java.lang.String',
    kind: 'property',
    label: 'common,security',
    order: 6,
    relation: [
      {
        action: 'ENABLE',
        when: [
          {
            id: 'skipCertificateCheck',
            value: 'false',
          },
        ],
      },
    ],
    required: false,
    secret: false,
    type: 'textarea',
  },
  brokerUrl: {
    componentProperty: true,
    deprecated: false,
    displayName: 'Broker URL',
    group: 'common',
    javaType: 'java.lang.String',
    kind: 'property',
    label: 'common',
    labelHint: 'Location to send data to or obtain data from.',
    order: 0,
    placeholder: 'for example, failover://ssl://{BROKER-HOST}:{BROKER-PORT}',
    required: true,
    secret: false,
    type: 'string',
  },
  clientID: {
    componentProperty: true,
    deprecated: false,
    displayName: 'Client ID',
    group: 'security',
    javaType: 'java.lang.String',
    kind: 'property',
    label: 'common,security',
    labelHint:
      'Required for connections to close and reopen without missing messages. Connection destination must be a topic.',
    order: 4,
    required: false,
    secret: false,
    type: 'string',
  },
  clientNumber: {
    componentProperty: true,
    controlHint: 'This thingie here has a tooltip just for the control',
    defaultValue: '5',
    deprecated: false,
    displayName: 'Client Number',
    group: 'security',
    javaType: 'java.lang.String',
    kind: 'property',
    label: 'common,security',
    labelHint:
      'Required for connections to close and reopen without missing messages. Connection destination must be a topic.',
    order: 4,
    required: false,
    secret: false,
    type: 'number',
  },
  password: {
    componentProperty: true,
    deprecated: false,
    displayName: 'Password',
    group: 'security',
    javaType: 'java.lang.String',
    kind: 'property',
    label: 'common,security',
    labelHint: 'Password for the specified user account.',
    order: 1,
    required: false,
    secret: true,
    type: 'string',
  },
  showAll: {
    componentProperty: false,
    defaultValue: 'true',
    deprecated: false,
    description: 'whether or not to log everything (very verbose).',
    displayName: 'Log everything',
    javaType: 'boolean',
    kind: 'parameter',
    labelHint: 'Some label hint or whatever',
    order: 2,
    required: false,
    secret: false,
    type: 'boolean',
  },
  skipCertificateCheck: {
    componentProperty: true,
    defaultValue: 'false',
    deprecated: false,
    displayName: 'Check Certificates',
    enum: [
      {
        label: 'Disable',
        value: 'true',
      },
      {
        label: 'Enable',
        value: 'false',
      },
    ],
    group: 'security',
    javaType: 'java.lang.String',
    kind: 'property',
    label: 'common,security',
    labelHint:
      'Ensure certificate checks are enabled for secure production environments. Disable for convenience in only development environments.',
    order: 5,
    required: false,
    secret: false,
    type: 'string',
  },
  someDuration: {
    description: 'How often to poll for updates',
    displayName: 'Polling Time',
    order: 3,
    type: 'duration',
  },
  username: {
    componentProperty: true,
    deprecated: false,
    displayName: 'User Name',
    group: 'security',
    javaType: 'java.lang.String',
    kind: 'property',
    label: 'common,security',
    labelHint: 'Access the broker with this user’s authorization credentials.',
    order: 2,
    required: false,
    secret: false,
    type: 'hidden',
  },
} as IFormDefinition;

export const initialValue = {
  brokerUrl: 'tcp://blah',
  password:
    '»ENC:c4b1c3818185a78d61f31d3b2bcba791d0e1dc6c5e691f4bf25f8513d6ee999b',
  skipCertificateCheck: 'false',
  someDuration: 45000,
  username: 'blah',
};

stories.add('Kitchen Sink', () => {
  const validate = v => {
    const errors: any = {};
    if (v.clientID === 'foo') {
      errors.clientID = 'Client ID cannot be set to "foo"';
    }
    return errors;
  };
  return (
    <StoryWrapper definition={definition}>
      <AutoForm
        definition={object('Definition', definition)}
        initialValue={object('Initial Value', initialValue)}
        i18nRequiredProperty={text(
          'i18nRequiredProperty',
          'This property is required'
        )}
        validate={validate}
        validateInitial={validate}
        onSave={(val, bag) => {
          bag.setSubmitting(false);
          action('onSave')(val);
        }}
      >
        {({ fields, handleSubmit }) => (
          <FormWrapper onSubmit={handleSubmit} fields={fields} />
        )}
      </AutoForm>
    </StoryWrapper>
  );
});
