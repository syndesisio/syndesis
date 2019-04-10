import * as React from 'react';
import { render } from 'react-testing-library';
import { AggregatedMetricCard } from '../src/Dashboard';

export default describe('AggregatedMetricCard', () => {
  const testComponent = (
    <AggregatedMetricCard title={'A Title'} ok={10} error={5} />
  );

  it('Should have the A Title title', function() {
    const { getByTestId } = render(testComponent);
    expect(getByTestId('aggregate-title')).toHaveTextContent('A Title');
  });
  it('Should have 5 errors', function() {
    const { getByTestId } = render(testComponent);
    expect(getByTestId('aggregate-error-count')).toHaveTextContent('5');
  });
  it('Should have 10 ok', function() {
    const { getByTestId } = render(testComponent);
    expect(getByTestId('aggregate-ok-count')).toHaveTextContent('10');
  });
});
