import * as React from 'react';
import { render } from 'react-testing-library';
import { AggregatedMetricCard } from '../../src/Shared';

export default describe('AggregatedMetricCard', () => {
  const testComponent = (
    <AggregatedMetricCard title={'A Title'} total={15} ok={10} error={5} />
  );

  it('Should have the A Title title', function() {
    const { getByTestId } = render(testComponent);
    expect(getByTestId('aggregated-metric-card-title')).toHaveTextContent(
      'A Title'
    );
  });
  it('Should have 5 errors', function() {
    const { getByTestId } = render(testComponent);
    expect(getByTestId('aggregated-metric-card-error-count')).toHaveTextContent(
      '5'
    );
  });
  it('Should have 10 ok', function() {
    const { getByTestId } = render(testComponent);
    expect(getByTestId('aggregated-metric-card-ok-count')).toHaveTextContent(
      '10'
    );
  });
  it('Should have 15 total', function() {
    const { getByTestId } = render(testComponent);
    expect(getByTestId('aggregated-metric-card-total-count')).toHaveTextContent(
      '15'
    );
  });
});
