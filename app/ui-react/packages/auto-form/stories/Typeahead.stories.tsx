import { action } from '@storybook/addon-actions';
import { object, text } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import {
  AutoForm
} from '../src';
import { FormWrapper } from './FormWrapper';
import { StoryWrapper } from './StoryWrapper';

const stories = storiesOf('Typeahead', module);

stories.add('Creatable Option', () => {
  const definition = {
    creatable: {
      displayName: 'Pick or name a thing',
      enum: [
        {
          label: 'One Fish',
          value: 'One Fish',
        },
        {
          label: 'Two Fish',
          value: 'Two Fish',
        },
        {
          label: 'Red Fish',
          value: 'Red Fish',
        },
        {
          label: 'Blue Fish',
          value: 'Blue Fish',
        },
      ],
      labelHint: 'This is shown for the label hint text',
      order: 0,
      type: 'typeahead',
    },
  };
  return (
    <StoryWrapper definition={definition}>
      <AutoForm
        definition={object('Definition', definition)}
        i18nRequiredProperty={text(
          'i18nRequiredProperty',
          'This property is required'
        )}
        initialValue={object('Initial Value', {
          creatable: 'Blue Fish'
        })}
        validate={action('validate')}
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


stories.add('Disabled Option', () => {
  const definition = {
    disabled: {
      displayName: 'Disabled',
      enum: [
        {
          label: 'Blue',
          value: 'blue'
        },
        {
          label: 'Red',
          value: 'red'
        }
      ],
      labelHint: 'Testing disabled select',
      order: 1,
      type: 'typeahead',
      disabled: true
    },
  };
  return (
    <StoryWrapper definition={definition}>
      <AutoForm
        definition={object('Definition', definition)}
        i18nRequiredProperty={text(
          'i18nRequiredProperty',
          'This property is required'
        )}
        initialValue={object('Initial Value', {
          disabled: 'blue'
        })}
        validate={action('validate')}
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
