import * as React from 'react';
import { render } from 'react-testing-library';
import { FormInputComponent } from '../src/widgets/FormInputComponent';

export default describe('FormInputComponent', () => {
  const textInputComponent = (
    <FormInputComponent
      form={{ isSubmitting: false }}
      field={{
        name: 'testTextInput',
        value: '',
        onChange: () => {},
      }}
      property={{
        displayName: 'Test Text Input Control Label',
        description: 'Test text description',
      }}
    />
  );

  it('Should use the definition key as an id for the textinput', () => {
    const { getByTestId } = render(textInputComponent);
    const idValue = textInputComponent.props.field.name;
    expect(getByTestId(idValue)).toBeDefined();
  });

  it('Should use the displayName as a label in the textinput', () => {
    const { getByLabelText } = render(textInputComponent);
    const displayName = textInputComponent.props.property.displayName;
    expect(getByLabelText(displayName)).toBeTruthy();
  });

  const pwdInputComponent = (
    <FormInputComponent
      type="password"
      form={{ isSubmitting: false }}
      field={{
        name: 'testPwdInput',
        value: '',
        onChange: () => {},
      }}
      property={{
        displayName: 'Test Pwd Input Control Label',
        description: 'Test password description',
      }}
    />
  );

  it('Should use the definition key as an id for the password input', () => {
    const { getByTestId } = render(pwdInputComponent);
    const idValue = pwdInputComponent.props.field.name;
    expect(getByTestId(idValue)).toBeDefined();
  });

  it('Should use the displayName as a label in the password input', () => {
    const { getByLabelText } = render(pwdInputComponent);
    const displayName = pwdInputComponent.props.property.displayName;
    expect(getByLabelText(displayName)).toBeTruthy();
  });

  const numberInputComponent = (
    <FormInputComponent
      type="number"
      form={{ isSubmitting: false }}
      field={{
        name: 'testNumInput',
        defaultValue: 2,
        min: 0,
        max: 3,
        onChange: () => {},
      }}
      property={{
        displayName: 'Test Pwd Input Control Label',
        description: 'Test password description',
      }}
    />
  );

  it('Should use the definition key as an id for the number input', () => {
    const { getByTestId } = render(numberInputComponent);
    const idValue = numberInputComponent.props.field.name;
    expect(getByTestId(idValue)).toBeDefined();
  });

  it('Should use the displayName as a label in the number input', () => {
    const { getByLabelText } = render(numberInputComponent);
    const displayName = numberInputComponent.props.property.displayName;
    expect(getByLabelText(displayName)).toBeTruthy();
  });

  it('Should set the value attr with defaultValue prop value', () => {
    const { getByTestId } = render(numberInputComponent);
    const defaultValue = String(numberInputComponent.props.field.defaultValue);
    const domValueAttr = getByTestId(
      numberInputComponent.props.field.name
    ).getAttribute('value');
    expect(defaultValue).toBe(domValueAttr);
  });
});
