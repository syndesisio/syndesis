import * as React from 'react';
import { render } from 'react-testing-library';
import { FormSelectComponent } from '../src/widgets/FormSelectComponent';

export default describe('FormSelectComponent', () => {
  const formSelectComponent = (
    <FormSelectComponent
      form={{ isSubmitting: false }}
      field={{
        name: 'test01Select',
        value: '',
        onChange: () => {},
      }}
      property={{
        displayName: 'Test Select Control Label',
        description: 'Test Select Description',
      }}
      validationState="warning"
    />
  );

  it('Should use the definition key as an id for the select', () => {
    const { getByTestId } = render(formSelectComponent);
    const idValue = formSelectComponent.props.field.name;
    expect(getByTestId(idValue)).toBeDefined();
  });

  it('Should use the displayName as a label in the select', () => {
    const { getByLabelText } = render(formSelectComponent);
    const displayName = formSelectComponent.props.property.displayName;
    expect(getByLabelText(displayName)).toBeTruthy();
  });

  it('Should set the proper css class when validationState prop is warning', () => {
    const { container } = render(formSelectComponent);
    const hasWarningClass =
      container.firstElementChild &&
      container.firstElementChild.classList.contains('has-warning');
    expect(hasWarningClass).toBe(true);
  });
});
