import { render } from '@testing-library/react';
import * as React from 'react';
import { MemoryRouter as Router } from 'react-router-dom';
import { WithRouter } from '../src';

export default describe('WithRouter', function() {
  const testComponent = (
    <Router>
      <WithRouter>{props => <span>ok</span>}</WithRouter>
    </Router>
  );

  it('Should render', function() {
    const { getByText } = render(testComponent);
    expect(getByText('ok')).toHaveTextContent('ok');
  });
});
