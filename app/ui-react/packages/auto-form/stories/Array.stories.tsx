import { action } from '@storybook/addon-actions';
import { object, text } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { AutoForm, IFormDefinition } from '../src';
import { FormWrapper } from './FormWrapper';
import { StoryWrapper } from './StoryWrapper';

const stories = storiesOf('AutoForm Arrays', module);

stories.add('General Example', () => {
  const definition = {
    ArrayOfThings: {
      arrayDefinition: {
        booleanField: {
          displayName: 'Boolean Field',
          order: 1,
          type: 'boolean',
        },
        numberField: {
          displayName: 'Number Field',
          order: 2,
          type: 'number',
        },
        stringField: {
          displayName: 'String Field',
          order: 0,
          type: 'text',
        },
      },
      arrayDefinitionOptions: {
        arrayControlAttributes: {
          className: 'form-array-section__action',
        },
        arrayRowTitleAttributes: {
          className: 'form-array-section__header',
        },
        formGroupAttributes: {
          className: 'col-md-3',
        },
        i18nAddElementText: 'Add another Thing',
        minElements: 2,
        rowTitle: 'Thingy',
        showSortControls: true,
      },
      type: 'array',
    },
  } as IFormDefinition;

  const initialValue = {
    ArrayOfThings: [
      { booleanField: false, numberField: 23, stringField: 'Hello!' },
      { booleanField: true, numberField: 48, stringField: 'Words' },
      { booleanField: true, numberField: 5232, stringField: 'More things' },
      {},
    ],
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

stories.add('Empty with Default Value', () => {
  const definition = {
    myArrayField: {
      arrayDefinition: {
        key: {
          displayName: 'Key',
          type: 'text',
        },
        value: {
          displayName: 'Value',
          type: 'text',
        },
      },
      arrayDefinitionOptions: {
        i18nAddElementText: 'Add Thing',
        minElements: 1,
        showSortControls: true,
      },
      defaultValue: [{ key: 'cheese1', value: 'cheddar' }] as any, // TODO - really need to make this interface typed
      displayName: 'Cheese Selections',
      type: 'array',
    },
  } as IFormDefinition;
  const initialValue = {};
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
