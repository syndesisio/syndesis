import * as React from 'react';
import { render } from 'react-testing-library';
import { AutoForm } from '../src';

export default describe('AutoForm', () => {
  const onSave = () => {
    // TODO
  };
  const testTextInputComponent = (
    <AutoForm
      definition={{
        foo: {
          defaultValue: 'bar',
          displayName: 'Foo',
          type: 'text',
        },
      }}
      initialValue={{}}
      i18nRequiredProperty={'required'}
      onSave={onSave}
    >
      {({ fields, handleSubmit }) => <React.Fragment>{fields}</React.Fragment>}
    </AutoForm>
  );

  const testCheckboxComponent = (
    <AutoForm
      definition={{
        showAll: {
          defaultValue: 'true',
          description: 'whether or not to log everything (very verbose).',
          displayName: 'Log everything',
          type: 'boolean',
        },
      }}
      initialValue={true}
      i18nRequiredProperty={'required'}
      onSave={onSave}
    >
      {({ fields, handleSubmit }) => <React.Fragment>{fields}</React.Fragment>}
    </AutoForm>
  );

  it('Should use the definition key as an id for text input', () => {
    const { getByTestId } = render(testTextInputComponent);
    const idValue = Object.keys(testTextInputComponent.props.definition);
    expect(getByTestId(idValue[0])).toBeDefined();
  });

  it('Should use the displayName as a label in text input', () => {
    const { getByLabelText } = render(testTextInputComponent);
    const displayName = testTextInputComponent.props.definition.foo.displayName;
    expect(getByLabelText(displayName)).toBeDefined();
  });

  it('Should use the definition key as an id for checkbox', () => {
    const { getByTestId } = render(testCheckboxComponent);
    const idValue = Object.keys(testCheckboxComponent.props.definition);
    expect(getByTestId(idValue[0])).toBeDefined();
  });

  it('Should use the displayName as a label in checkbox', () => {
    const { getByLabelText } = render(testCheckboxComponent);
    const displayName =
      testCheckboxComponent.props.definition.showAll.displayName;
    expect(getByLabelText(displayName)).toBeTruthy();
  });
});
