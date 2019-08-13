import { action } from '@storybook/addon-actions';
import { number, object, text, withKnobs } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { AutoForm } from '../src';
import { FormWrapper } from './FormWrapper';
import { StoryWrapper } from './StoryWrapper';

const stories = storiesOf('AutoForm Simple Examples', module);
stories.addDecorator(withKnobs);

stories.add('Minimal Form Definition', () => {
  const definition = {
    SomeSelect: {
      displayName: 'Pick Something',
      enum: [
        {
          label: 'Broken Leg',
          value: '1',
        },
        {
          label: 'Broken Arm',
          value: '2',
        },
        {
          label: 'Upset Stomach',
          value: '3',
        },
        {
          label: 'Measles',
          value: '4',
        },
      ],
      order: 1,
      type: 'string',
    },
    SomeTextField: {
      displayName: 'Some Text Field',
      order: 0,
      type: 'string',
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
          SomeTextField: 'Some Value',
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

stories.add('Text', () => {
  const definition = {
    SomeTextField: {
      displayName: text('Display Name', 'The Label'),
      labelHint: 'This is shown for the label hint text',
      placeholder: 'This is the placeholder text',
      type: 'string',
    },
    SomeTextFieldDataList: {
      dataList: ['Some thing', 'Something else', 'Stuff'],
      displayName: text('Display Name', 'The Label'),
      labelHint: 'This is shown for the label hint text',
      placeholder: 'This is the placeholder text',
      type: 'string',
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
          SomeTextField: 'Some Value',
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

stories.add('Number', () => {
  const definition = {
    SomeNumberField: {
      displayName: text('Display Name', 'The Label'),
      labelHint: 'This is shown for the label hint text',
      type: 'number',
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
          SomeNumberField: 55,
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

stories.add('Checkbox', () => {
  const definition = {
    SomeBooleanField: {
      displayName: 'Boolean Value',
      labelHint: 'This is shown for the label hint text',
      type: 'boolean',
    },
    SomeOtherBooleanField: {
      displayName: `String containing "false"`,
      labelHint: 'This is shown for the label hint text',
      type: 'boolean',
    },
    SomeThirdBooleanField: {
      displayName: `String containing "true"`,
      labelHint: 'This is shown for the label hint text',
      type: 'boolean',
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
          SomeBooleanField: true,
          SomeOtherBooleanField: 'false',
          SomeThirdBooleanField: 'True',
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

stories.add('Textarea', () => {
  const definition = {
    SomeTextField: {
      displayName: text('Display Name', 'The Label'),
      fieldAttributes: {
        cols: 10,
        rows: 10,
      },
      labelHint: 'This is shown for the label hint text',
      placeholder: 'This is the placeholder text',
      type: 'textarea',
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
          SomeTextField:
            'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed sollicitudin dolor purus, id pharetra augue maximus efficitur. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Phasellus elementum tortor sem, ut vulputate sapien tristique in. Curabitur venenatis mauris nunc, ut varius libero tincidunt sed. Aliquam porttitor viverra faucibus. Curabitur sodales nisi sem, id pulvinar ante luctus eget. Cras vitae ligula pretium felis varius pulvinar. Phasellus vel sem gravida diam venenatis cursus quis sed nisi. Cras a erat eget nibh consequat feugiat nec et enim. Sed finibus tristique diam, non sodales nulla elementum vel. Curabitur cursus lacus vel vestibulum scelerisque. Curabitur tellus sapien, pretium in metus non, fringilla consequat neque.',
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

stories.add('Select', () => {
  const definition = {
    MultiField: {
      displayName: 'Pick things',
      enum: [
        {
          label: 'One Fish',
          value: 'one',
        },
        {
          label: 'Two Fish',
          value: 'two',
        },
        {
          label: 'Red Fish',
          value: 'three',
        },
        {
          label: 'Blue Fish',
          value: 'four',
        },
      ],
      fieldAttributes: {
        multiple: true,
      },
      order: 2,
      type: 'string',
    },
    SomeField: {
      displayName: 'Pick a thing',
      enum: [
        {
          label: 'One Fish',
          value: 'one',
        },
        {
          label: 'Two Fish',
          value: 'two',
        },
        {
          label: 'Red Fish',
          value: 'three',
        },
        {
          label: 'Blue Fish',
          value: 'four',
        },
      ],
      labelHint: 'This is shown for the label hint text',
      order: 1,
      type: 'string',
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
          MultiField: ['one', 'four'],
          SomeField: 'three',
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

stories.add('Duration', () => {
  const definition = {
    duration1: {
      displayName: 'Duration 1',
      labelHint: 'This is shown for the label hint text',
      type: 'duration',
    },
    duration2: {
      description: 'Some description so the help text is filled in',
      displayName: 'Duration 2',
      labelHint: 'This is shown for the label hint text',
      type: 'duration',
    },
    duration3: {
      displayName: 'Duration 3',
      labelHint: 'This is shown for the label hint text',
      type: 'duration',
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
          duration1: number('Duration 1', 5 * 1000 * 60 * 60),
          duration2: number('Duration 2', 100),
          duration3: number('Duration 3', 2 * 1000 * 60),
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
