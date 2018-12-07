import { object, text } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { StoryHelper } from '../.storybook/StoryHelper';
import { AutoForm, IFormDefinition } from '../src';

const stories = storiesOf('Components', module);

const formDefinition = {
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
  username: 'blah',
};

stories
  .addDecorator(story => <StoryHelper>{story()}</StoryHelper>)
  .add('AutoForm', () => {
    const onSave = v => {
      alert('Got value: ' + JSON.stringify(v, undefined, 2));
    };
    const validate = v => {
      const errors: any = {};
      if (v.clientID === 'foo') {
        errors.clientID = 'Client ID cannot be set to "foo"';
      }
      return errors;
    };
    return (
      <AutoForm
        definition={object('definition', formDefinition)}
        initialValue={object('initialValue', initialValue)}
        i18nRequiredProperty={text(
          'i18nRequiredProperty',
          'This property is required'
        )}
        validate={validate}
        onSave={onSave}
      >
        {({ fields, handleSubmit }) => (
          <React.Fragment>
            <p className="fields-status-pf">
              The fields marked with <span className="required-pf">*</span> are
              required.
            </p>
            {fields}
            <button
              type="button"
              className="btn btn-primary"
              onClick={handleSubmit}
            >
              Submit
            </button>
          </React.Fragment>
        )}
      </AutoForm>
    );
  });
