import * as React from 'react';
import { render } from 'react-testing-library';
import { AutoForm, IFormDefinition, toValidHtmlId } from '../src';

describe('FormArrayComponent', () => {
  const fieldId = 'testArrayInput';
  const definition = {
    [fieldId]: {
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
  const getForm = (definition: IFormDefinition, initialValue: any) => (
    <AutoForm
      definition={definition}
      initialValue={initialValue}
      i18nRequiredProperty={'required'}
      onSave={() => false}
    >
      {({ fields }) => fields}
    </AutoForm>
  );

  it('Should use the definition key as an id', () => {
    const { getByTestId } = render(getForm(definition, {}));
    expect(getByTestId(toValidHtmlId(fieldId))).toBeDefined();
  });

  it('Should render set to the default value', () => {
    const { getByTestId } = render(getForm(definition, {}));
    expect(
      (getByTestId(toValidHtmlId(`${fieldId}[0].value`)) as HTMLInputElement)
        .value
    ).toEqual('cheddar');
  });

  it('Should render set to the initial value', () => {
    const { getByTestId } = render(
      getForm(definition, {
        [fieldId]: [
          { key: 'cheese1', value: 'cheddar' },
          { key: 'cheese2', value: 'colby' },
        ],
      })
    );
    expect(
      (getByTestId(toValidHtmlId(`${fieldId}[1].value`)) as HTMLInputElement)
        .value
    ).toEqual('colby');
  });
});
