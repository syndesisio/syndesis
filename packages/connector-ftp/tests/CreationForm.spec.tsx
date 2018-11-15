import { render } from 'react-testing-library';
import * as React from 'react';
import { CreationForm } from '../src';

export default describe('CreationForm', function() {
  const testComponent = <CreationForm />;

  it('Should render', function() {
    const { getByText } = render(testComponent);
    expect(getByText('FTP Configuration')).toHaveTextContent(
      'FTP Configuration'
    );
  });
});
