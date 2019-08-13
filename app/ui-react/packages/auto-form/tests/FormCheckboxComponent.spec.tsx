import * as React from 'react';
import { render } from 'react-testing-library';
import { AutoForm, toValidHtmlId } from '../src';

export default describe('FormCheckboxComponent', () => {
  const fieldId = 'test01TestCheckbox';
  const definition = {
    [fieldId]: {
      defaultValue: 'true',
      description: 'whether or not to log everything (very verbose).',
      displayName: 'Log everything',
      required: true,
      secret: false,
      type: 'boolean',
    },
  };
  const form = (
    <AutoForm
      definition={definition}
      initialValue={{}}
      i18nRequiredProperty={'required'}
      onSave={() => false}
    >
      {({ fields }) => fields}
    </AutoForm>
  );

  it('Should use the definition key as an id for the checkbox', () => {
    const { getByTestId } = render(form);
    expect(getByTestId(toValidHtmlId(fieldId))).toBeDefined();
  });

  it('Should use the displayName as a label in the checkbox', () => {
    const { getByLabelText } = render(form);
    const displayName = definition[fieldId].displayName;
    expect(getByLabelText(displayName)).toBeTruthy();
  });

  it('Should render set to the default value', () => {
    const { getByTestId } = render(form);
    expect(
      (getByTestId(toValidHtmlId(fieldId)) as HTMLInputElement).value
    ).toEqual("on"); // pf4 react checkbox sets this to 'on'
  });
});
