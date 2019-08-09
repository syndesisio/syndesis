import { action } from '@storybook/addon-actions';
import { object, text } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { AutoForm, IFormDefinition } from '../src';
import { FormWrapper } from './FormWrapper';
import { StoryWrapper } from './StoryWrapper';

const stories = storiesOf('Syndesis AutoForm UIs', module);

stories.add('API Provider Return Configuration', () => {
  const errors = [
    { displayName: 'SqlDataAccessError', name: 'SQL_DATA_ACCESS_ERROR' },
    {
      displayName: 'SqlEntityNotFoundError',
      name: 'SQL_ENTITY_NOT_FOUND_ERROR',
    },
    { displayName: 'SqlConnectorError', name: 'SQL_CONNECTOR_ERROR' },
  ];

  const responseCodes = [
    { label: '200 OK', value: '200' },
    { label: '500 Internal Server Error', value: '500' },
    { label: '404 Not Found', value: '404' },
  ];

  const definition = {
    defaultResponse: {
      displayName: 'Default Response',
      order: 0,
      type: 'legend',
    },
    errorHandling: {
      displayName: 'Error Handling',
      order: 2,
      type: 'legend',
    },
    errorResponseCodes: {
      componentProperty: false,
      defaultValue: '{}',
      description:
        'The return code to set according to different error situations',
      displayName: 'Error Response Codes',
      mapsetKeys: [...errors],
      mapsetValueDefinition: {
        displayName: 'Status Code',
        enum: [{ label: 'Use Default', value: '0' }, ...responseCodes],
        type: 'select',
      },
      order: 4,
      required: false,
      type: 'mapset',
    },
    httpResponseCode: {
      displayName: 'Specify HTTP Response Code',
      enum: [...responseCodes],
      order: 1,
      type: 'select',
    },
    returnBody: {
      displayName: 'Include error message in the return body',
      order: 3,
      type: 'boolean',
    },
  } as IFormDefinition;

  const initialValue = {
    errorResponseCodes: {
      SQL_CONNECTOR_ERROR: { status: '500' },
      SQL_DATA_ACCESS_ERROR: { status: '403' },
      SQL_ENTITY_NOT_FOUND_ERROR: { status: '404' },
    },
    httpResponseCode: '500',
    returnBody: false,
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
        validate={action('validate')}
        validateInitial={action('validateInitial')}
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

stories.add('Editor Basic Filter', () => {
  const definition = {
    predicate: {
      defaultValue: 'AND',
      displayName: 'Continue only if incoming data match ',
      enum: [
        {
          label: 'ALL of the following',
          value: 'AND',
        },

        {
          label: 'ANY of the following',
          value: 'OR',
        },
      ],
      type: 'select',
    },
    rules: {
      arrayDefinition: {
        op: {
          defaultValue: 'contains',
          description: 'Must meet this condition',
          displayName: 'Operator',
          enum: [
            {
              label: 'equals',
              operator: '==',
            },
            {
              label: 'equals (ignores case)',
              operator: '=~',
            },
            {
              label: 'not equals',
              operator: '!=',
            },
            {
              label: 'less than',
              operator: '<',
            },
            {
              label: 'less than or equal to',
              operator: '<=',
            },
            {
              label: 'greater than',
              operator: '>',
            },
            {
              label: 'greater than or equal to',
              operator: '>=',
            },
            {
              label: 'contains',
              operator: 'contains',
            },
            {
              label: 'contains (ignore case)',
              operator: '~~',
            },
            {
              label: 'not contains',
              operator: 'not contains',
            },
            {
              label: 'matches',
              operator: 'regex',
            },
            {
              label: 'not matches',
              operator: 'not regex',
            },
            {
              label: 'in',
              operator: 'in',
            },
            {
              label: 'not in',
              operator: 'not in',
            },
          ],
          order: 1,
          required: true,
          type: 'text',
        },
        path: {
          dataList: ['foo', 'bar', 'cheese'],
          description: 'The data you want to evaluate',
          displayName: 'Property Name',
          order: 0,
          placeholder: 'Property name',
          required: true,
          type: 'text',
        },
        value: {
          description: 'For this value',
          displayName: 'Keywords',
          order: 2,
          placeholder: 'Keywords',
          required: true,
          type: 'text',
        },
      },
      arrayDefinitionOptions: {
        arrayControlAttributes: {
          className: 'form-group with-rule-filter-form__action',
        },
        formGroupAttributes: {
          className: 'with-rule-filter-form__group',
        },
        i18nAddElementText: '+ Add another rule',
        minElements: 1,
      },
      required: true,
      type: 'array',
    },
    type: {
      defaultValue: 'rule',
      type: 'hidden',
    },
  };
  const initialValue = {
    predicate: 'AND',
    rules: [
      {
        op: 'contains',
        path: 'bar',
        value: 'some stuff',
      },
      {
        op: 'matches',
        path: 'cheese',
        value: '*',
      },
    ],
    type: 'rule',
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
        validate={action('validate')}
        validateInitial={action('validateInitial')}
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
