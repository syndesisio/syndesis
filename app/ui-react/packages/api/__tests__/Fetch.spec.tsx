import { render } from '@testing-library/react';
import * as React from 'react';
import { Fetch } from '../src/Fetch';

export default describe('Fetch', function() {
  const testComponent = (
    <Fetch baseUrl={'http://example.com'} url={'/test'} defaultValue={{}}>
      {props => <span>ok</span>}
    </Fetch>
  );

  it('Should render', function() {
    const { getByText } = render(testComponent);
    expect(getByText('ok')).toHaveTextContent('ok');
  });
});
