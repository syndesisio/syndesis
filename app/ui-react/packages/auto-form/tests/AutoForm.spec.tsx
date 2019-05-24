import * as React from 'react';
import { fireEvent, render, wait } from 'react-testing-library';
import { AutoForm, toValidHtmlId } from '../src';

export default describe('AutoForm', () => {
  const definitions = {
    text: {
      defaultValue: 'INFO',
      description: 'Log Level.',
      displayName: 'log level',
      kind: 'parameter',
      required: true,
      secret: false,
      type: 'string',
    },
    select: {
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
    checkbox: {
      defaultValue: 'false',
      description: 'whether or not to log everything (very verbose).',
      displayName: 'Log everything',
      required: true,
      secret: false,
      type: 'boolean',
    },
  };
  const definitionIds = Object.keys(definitions);

  const testSampleForm = definition => {
    const onSave = jest.fn();
    return {
      form: (
        <AutoForm
          definition={definition}
          initialValue={{}}
          i18nRequiredProperty={'required'}
          onSave={onSave}
        >
          {({ fields, handleSubmit }) => (
            <form onSubmit={handleSubmit}>
              {fields}
              <button type={'submit'}>Submit</button>
            </form>
          )}
        </AutoForm>
      ),
      onSave,
    };
  };

  it('All fields are rendered', () => {
    const { form } = testSampleForm(definitions);
    const { getByTestId } = render(form);
    definitionIds.forEach(id =>
      expect(getByTestId(toValidHtmlId(id))).toBeDefined()
    );
  });

  definitionIds.forEach(id =>
    it(`An untouched ${id} with default values should be submittable`, async () => {
      const { form, onSave } = testSampleForm(definitions[id]);
      const { getByText } = render(form);
      fireEvent.click(getByText('Submit'));
      await wait(() => {
        expect(onSave).toHaveBeenCalled();
      });
    })
  );

  it(`An untouched complex form with valid default values should be submittable`, async () => {
    const { form, onSave } = testSampleForm(definitions);
    const { getByText } = render(form);
    fireEvent.click(getByText('Submit'));
    await wait(() => {
      expect(onSave).toHaveBeenCalled();
    });
  });
});
