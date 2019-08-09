import * as React from 'react';
import { render } from 'react-testing-library';
import { AutoForm, toValidHtmlId } from '../src';

describe('FormTextareaComponent', () => {
  const fieldId = 'testTextareaInput';
  const definition = {
    [fieldId]: {
      defaultValue:
        'Lorem ipsum dolor sit amet, consectetur adipisicing elit. Eaque eius non quae. Neque, repellat, repudiandae! Enim possimus quam vel? Accusantium deleniti facilis fuga fugiat nihil perferendis quam quibusdam vitae? Error!',
      description: 'Textarea',
      displayName: 'Textarea',
      kind: 'parameter',
      required: true,
      secret: false,
      type: 'textarea',
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
      (getByTestId(toValidHtmlId(fieldId)) as HTMLTextAreaElement).value.substr(-6)
    ).toEqual('Error!');
  });
});
