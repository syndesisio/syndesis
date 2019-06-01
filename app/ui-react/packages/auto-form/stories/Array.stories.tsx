import { action } from '@storybook/addon-actions';
import { object, text } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { AutoForm, IFormDefinition } from '../src';
import { FormWrapper } from './FormWrapper';
import { StoryWrapper } from './StoryWrapper';

const stories = storiesOf('AutoForm', module);

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
        className: 'col-md-1',
      },
      arrayRowTitleAttributes: {
        className: 'col-md-2',
      },
      formGroupAttributes: {
        className: 'col-md-3',
      },
      i18nAddElementText: '+ Add another Thing',
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

stories.add('Array', () => {
  return (
    <StoryWrapper>
      <AutoForm
        definition={object('Definition', definition)}
        initialValue={object('Initial Value', initialValue)}
        i18nRequiredProperty={text(
          'i18nRequiredProperty',
          'This property is required'
        )}
        validate={action('validate')}
        validateInitial={action('validateInitial')}
        onSave={action('onSave')}
      >
        {({ fields, handleSubmit }) => (
          <FormWrapper onSubmit={handleSubmit} fields={fields} />
        )}
      </AutoForm>
    </StoryWrapper>
  );
});
