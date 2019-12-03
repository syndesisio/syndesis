import * as React from 'react';
import { render } from '@testing-library/react';
import { AutoForm, toValidHtmlId } from '../src';

describe('FormInputComponent: text', () => {
  const fieldId = 'testTextInput';
  const definition = {
    [fieldId]: {
      defaultValue: 'INFO',
      description: 'Log Level.',
      displayName: 'log level',
      kind: 'parameter',
      required: true,
      secret: false,
      type: 'string',
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

  it('Should use the definition key as an id', () => {
    const { getByTestId } = render(form);
    expect(getByTestId(toValidHtmlId(fieldId))).toBeDefined();
  });

  it('Should use the displayName as a label', () => {
    const { getByLabelText } = render(form);
    const displayName = definition[fieldId].displayName;
    expect(getByLabelText(displayName)).toBeTruthy();
  });

  it('Should render set to the default value', () => {
    const { getByTestId } = render(form);
    expect(
      (getByTestId(toValidHtmlId(fieldId)) as HTMLInputElement).value
    ).toEqual('INFO');
  });
});

describe('FormInputComponent: password', () => {
  const fieldId = 'testPasswordInput';
  const definition = {
    [fieldId]: {
      defaultValue: 'supersecret',
      description: 'Password.',
      displayName: 'Password',
      kind: 'parameter',
      required: true,
      secret: true,
      type: 'string',
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

  it('Should use the definition key as an id', () => {
    const { getByTestId } = render(form);
    expect(getByTestId(toValidHtmlId(fieldId))).toBeDefined();
  });

  it('Should use the displayName as a label', () => {
    const { getByLabelText } = render(form);
    const displayName = definition[fieldId].displayName;
    expect(getByLabelText(displayName)).toBeTruthy();
  });

  it('Should render set to the default value', () => {
    const { getByTestId } = render(form);
    expect(
      (getByTestId(toValidHtmlId(fieldId)) as HTMLInputElement).value
    ).toEqual('supersecret');
  });
});

describe('FormInputComponent: number', () => {
  const fieldId = 'testNumberInput';
  const definition = {
    [fieldId]: {
      defaultValue: '123',
      description: 'Number.',
      displayName: 'Number',
      kind: 'parameter',
      required: true,
      secret: false,
      type: 'number',
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

  it('Should use the definition key as an id', () => {
    const { getByTestId } = render(form);
    expect(getByTestId(toValidHtmlId(fieldId))).toBeDefined();
  });

  it('Should use the displayName as a label', () => {
    const { getByLabelText } = render(form);
    const displayName = definition[fieldId].displayName;
    expect(getByLabelText(displayName)).toBeTruthy();
  });

  it('Should render set to the default value', () => {
    const { getByTestId } = render(form);
    expect(
      (getByTestId(toValidHtmlId(fieldId)) as HTMLInputElement).value
    ).toEqual('123');
  });
});
