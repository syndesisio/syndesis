import * as React from 'react';
import { render } from 'react-testing-library';
import { DataMapper } from '../src';

export default describe('DataMapper', () => {
  const testComponent = <DataMapper />;

  it('Should render', () => {
    const { container } = render(testComponent);
    expect(container).toBeDefined();
  });
});
