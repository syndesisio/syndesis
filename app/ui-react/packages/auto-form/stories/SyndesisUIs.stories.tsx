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
    { displayName: 'Server Error', name: 'SERVER_ERROR' },
    {
      displayName: 'Data Access Error',
      name: 'SQL_DATA_ACCESS_ERROR',
    },
    {
      displayName: 'Entity not found error',
      name: 'SQL_ENTITY_NOT_FOUND_ERROR',
    },
    {
      displayName: 'SQL connector error',
      name: 'SQL_CONNECTOR_ERROR',
    },
    {
      displayName: 'Twitter connector error',
      name: 'TWITTER_CONNECTOR_ERROR',
    },
    {
      displayName: 'Salesforce connector error',
      name: 'SALESFORCE_CONNECTOR_ERROR',
    },
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
      defaultValue: '{}',
      displayName: 'Assign HTTP response code to errors',
      labelHint:
        'The return code to set according to different error situations',
      mapsetKeys: [...errors],
      mapsetOptions: {
        i18nKeyColumnTitle: 'When the error message is',
        i18nValueColumnTitle: 'Return this HTTP response code',
      },
      mapsetValueDefinition: {
        enum: [{ label: 'Use Default Response', value: '0' }, ...responseCodes],
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
      displayNameCheckbox: 'Select return body preference',
      order: 3,
      type: 'boolean',
    },
  } as IFormDefinition;

  const initialValue = {
    errorResponseCodes: {
      SQL_CONNECTOR_ERROR: '500',
      SQL_DATA_ACCESS_ERROR: '403',
      SQL_ENTITY_NOT_FOUND_ERROR: '404',
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

stories.add('Conditional Flow', () => {
  const definition = {
    defaultFlowId: {
      order: 6,
      type: 'hidden',
    },
    flowConditions: {
      arrayDefinition: {
        condition: {
          defaultValue: '',
          description:
            "Provide a condition that you want to evaluate (for example, ${in.header.type} == 'note' or ${in.body.title} contains 'Important').",
          displayName: '',
          placeholder: 'Simple language expression',
          required: true,
          type: 'text',
        },
        flowId: {
          defaultValue: '',
          formGroupAttributes: {
            style: {
              display: 'none',
            },
          },
          type: 'hidden',
        },
      },
      arrayDefinitionOptions: {
        arrayControlAttributes: {
          className: 'conditional-flow__controls',
        },
        arrayRowTitleAttributes: {
          className: 'conditional-flow__title',
        },
        controlLabelAttributes: {
          style: { display: 'none' },
        },
        formGroupAttributes: {
          className: 'conditional-flow__form-group',
        },
        i18nAddElementText: 'Add another condition',
        minElements: 1,
        rowTitle: 'When',
        showSortControls: true,
      },
      order: 1,
      required: true,
      type: 'array',
    },
    forAllIncomingData: {
      displayName: 'For all incoming data',
      order: 0,
      type: 'legend',
    },
    otherwise: {
      displayName: 'If incoming data does not match all of the above conditions',
      order: 2,
      type: 'legend'
    },
    routingScheme: {
      defaultValue: 'direct',
      order: 5,
      type: 'hidden',
    },
    useDefaultFlow: {
      defaultValue: 'false',
      displayName: 'Execute default flow',
      order: 3,
      type: 'boolean',
    },
  } as IFormDefinition;

  const initialValue = {
    flowConditions: [
      {
        condition: '${in.body.type} == male',
        flowId: '-LlIzP2k_dR0uDy9VSrt',
      },
      {
        condition: '${in.body.type} == female',
        flowId: '-LlIzP2k_dR0uDy9VSrw',
      },
    ],
    routingScheme: 'direct',
    useDefaultFlow: true,
  };

  const validateAction = action('validate');

  const handleValidate = values => {
    const errors = {};

    values.flowConditions.forEach((value: any, index: number) => {
      if (!value.condition || value.condition === '') {
        errors[`flowConditions[${index}].condition`] = 'Condition is required';
      }
    });

    validateAction(values, errors);
    return errors;
  };

  return (
    <StoryWrapper definition={definition}>
      <style dangerouslySetInnerHTML={{__html: `
        .conditional-flow__form section {
          display: flex;
          flex-wrap: nowrap;
          flex-direction: row;
          margin-left: 7em;
        }
        .conditional-flow__form-group {
          padding: 0 15px 0 15px;
        }
        .conditional-flow__controls {
          padding-top: 4px;
        }
        .conditional-flow__form .conditional-flow__form-group .pf-c-form__group {
          display: inherit !important;
        }
        .conditional-flow__title {
          padding-top: 10px;
        }
        .conditional-flow__form .form-array-control__array-add {
          display: block;
          padding-left: 6.5em;
        }
        .conditional-flow__form .form-array-control__array-add .btn {
        
        }
      `}}/>
      <AutoForm
        definition={object('Definition', definition)}
        initialValue={object('Initial Value', initialValue)}
        i18nRequiredProperty={text(
          'i18nRequiredProperty',
          'This property is required'
        )}
        validate={handleValidate}
        validateInitial={action('validateInitial')}
        onSave={(val, bag) => {
          bag.setSubmitting(false);
          action('onSave')(val);
        }}
      >
        {({ fields, handleSubmit }) => (
          <FormWrapper className={'conditional-flow__form'} onSubmit={handleSubmit} fields={fields} />
        )}
      </AutoForm>
    </StoryWrapper>
  );
});
