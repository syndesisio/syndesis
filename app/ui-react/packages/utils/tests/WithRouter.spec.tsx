import { render } from 'react-testing-library';
import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
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
