import { action } from '@storybook/addon-actions';
import { object, text } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { AutoForm } from '@syndesis/auto-form';
import { ActionGroup, Button, Form } from '@patternfly/react-core';

import {
  Card,
  CardBody,
  CardHeader,
  Grid,
  GridItem,
  PageSection,
} from '@patternfly/react-core';

const stories = storiesOf('Connection/Kafka Broker Selector', module);

stories.add('Multi Select 1', () => {
  const definition = {
    rules: {
      arrayDefinition: {
        path: {
          dataList: ['foo', 'bar', 'cheese'],
          description: '* TLS is not currently supported',
          displayName: 'Kafka Broker URIs',
          order: 0,
          placeholder: 'Property name',
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
        i18nAddElementText: '+ Add another cluster',
        minElements: 1,
      },
      required: true,
      type: 'array',
    },
    type: {
      //defaultValue: 'rule',
      type: 'hidden',
    },
  };

  const initialValue = {
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
    //type: 'rule',
  };

  return (
    <PageSection>
      <Grid gutter={'md'}>
        <GridItem span={6}>
          <Card>
            <CardHeader>Kafka Message Broker</CardHeader>
            <CardBody>
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
                  <Form
                    isHorizontal={true}
                    className={''}
                    onSubmit={handleSubmit}
                  >
                    {fields}
                    <ActionGroup className="form-array-action">
                      <Button type={'submit'} variant={'primary'}>
                        Submit
                      </Button>
                      <Button variant={'secondary'}>Cancel</Button>
                    </ActionGroup>
                  </Form>
                )}
              </AutoForm>
            </CardBody>
          </Card>
        </GridItem>
      </Grid>
    </PageSection>
  );
});

stories.add('Multi Select 2', () => {
  return (
    <p>Coming soon.</p>
  );
});

stories.add('Multi Select 3', () => {
  return (
    <p>Coming soon.</p>
  );
});



