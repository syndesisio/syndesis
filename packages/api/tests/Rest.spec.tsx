import { render } from 'react-testing-library';
import * as React from 'react';
import { Rest } from '../src';

export default describe('Rest', function() {
  const testComponent = (
    <Rest
      baseUrl={'http://example.com'}
      url={'/test'}
      defaultValue={{}}
      autoload={false}
    >
      {props => <span>ok</span>}
    </Rest>
  );

  it('Should render', function() {
    const { getByText } = render(testComponent);
    expect(getByText('ok')).toHaveTextContent('ok');
  });
});
