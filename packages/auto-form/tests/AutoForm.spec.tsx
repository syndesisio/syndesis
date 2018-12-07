import * as React from 'react';
import { render } from 'react-testing-library';
import { AutoForm } from '../src';

export default describe('AutoForm', () => {
  const onSave = () => {
    // TODO
  };
  const testComponent = (
    <AutoForm
      definition={{
        foo: {
          defaultValue: 'bar',
          displayName: 'Foo',
          type: 'text',
        },
      }}
      i18nRequiredProperty={'required'}
      onSave={onSave}
    >
      {({ fields, handleSubmit }) => <React.Fragment>{fields}</React.Fragment>}
    </AutoForm>
  );

  it('Should have an input', () => {
    const { queryByTestId } = render(testComponent);
    expect(queryByTestId('foo')).toBeDefined();
  });
});
