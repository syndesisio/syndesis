import * as React from 'react';
import { render } from 'react-testing-library';
import { FormCheckboxComponent } from '../src/widgets/FormCheckboxComponent';

export default describe('FormCheckboxComponent', () => {
  const formCheckboxComponent = (
    <FormCheckboxComponent
      form={{ isSubmitting: false }}
      field={{
        name: 'test01TestCheckbox',
      }}
      property={{
        displayName: 'Test CB Control Label',
        description: 'Test CB dontrol description',
      }}
      validationState="error"
    />
  );

  it('Should use the definition key as an id for the checkbox', () => {
    const { getByTestId } = render(formCheckboxComponent);
    const idValue = formCheckboxComponent.props.field.name;
    expect(getByTestId(idValue)).toBeDefined();
  });

  it('Should use the displayName as a label in the checkbox', () => {
    const { getByLabelText } = render(formCheckboxComponent);
    const displayName = formCheckboxComponent.props.property.displayName;
    expect(getByLabelText(displayName)).toBeTruthy();
  });

  it('Should contain has-error class when validation state is error', () => {
    const { container } = render(formCheckboxComponent);
    const hasErrorClass =
      container.firstElementChild &&
      container.firstElementChild.classList.contains('has-error');
    expect(hasErrorClass).toBe(true);
  });
});
