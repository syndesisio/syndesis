import * as React from 'react';
import { render } from 'react-testing-library';
import { AutoForm, toValidHtmlId } from '../src';

export default describe('FormSelectComponent', () => {
  const fieldId = 'test01Select';
  const definition = {
    [fieldId]: {
      defaultValue: 'INFO',
      description: 'Log Level.',
      displayName: 'log level',
      kind: 'parameter',
      required: true,
      secret: false,
      type: 'string',
      enum: [
        {
          label: 'ERROR',
          value: 'ERROR',
        },
        {
          label: 'WARN',
          value: 'WARN',
        },
        {
          label: 'INFO',
          value: 'INFO',
        },
        {
          label: 'DEBUG',
          value: 'DEBUG',
        },
        {
          label: 'TRACE',
          value: 'TRACE',
        },
        {
          label: 'OFF',
          value: 'OFF',
        },
      ],
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
});
