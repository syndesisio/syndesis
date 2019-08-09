import * as React from 'react';
import { render } from 'react-testing-library';
import { AutoForm, IFormDefinition, toValidHtmlId } from '../src';

describe('FormDurationComponent', () => {
  const fieldId = 'testDurationInput';
  const definition = {
    [fieldId]: {
      defaultValue: '30000',
      description: 'Length of time to wait around',
      displayName: 'Wait Time',
      type: 'duration',
    },
  };
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

  it('Should use the displayName as a label', () => {
    const { getByLabelText } = render(getForm(definition, {}));
    const displayName = definition[fieldId].displayName;
    expect(getByLabelText(displayName)).toBeTruthy();
  });

  it('Should render set to the default value', () => {
    const { getByTestId } = render(getForm(definition, {}));
    expect(
      (getByTestId(toValidHtmlId(fieldId)) as HTMLInputElement).value
    ).toEqual("30");
    expect(
      (getByTestId(toValidHtmlId(`${fieldId}-duration`)) as HTMLButtonElement).textContent!.trim()
    ).toEqual('Seconds');
  });

  it('Should render set to the initial value', () => {
    const { getByTestId } = render(getForm(definition, { [fieldId]: 60000}));
    expect(
      (getByTestId(toValidHtmlId(fieldId)) as HTMLInputElement).value
    ).toEqual("1");
    expect(
      (getByTestId(toValidHtmlId(`${fieldId}-duration`)) as HTMLButtonElement).textContent!.trim()
    ).toEqual('Minutes');
  });
});
