import { render } from 'react-testing-library';
import * as React from 'react';
import { App } from './App';

export default describe('App', () => {
  const testComponent = <App />;

  it('Should render', () => {
    const { container } = render(testComponent);
    expect(container).toBeDefined();
  });
});
