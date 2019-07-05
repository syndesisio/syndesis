import * as React from 'react';
import 'jest-dom/extend-expect';
import { render } from 'react-testing-library';
import { IntegrationStatusDetail } from '../../src/Integration';

export default describe('IntegrationStatusDetail', () => {
  // Publishing state wwith no details
  const testComponentPublishing = (
    <IntegrationStatusDetail
      targetState={'Published'}
      i18nProgressPending={'Pending'}
      i18nProgressStarting={'Starting...'}
      i18nProgressStopping={'Stopping...'}
      i18nLogUrlText={'View Log'}
    />
  );

  const testComponentPublishingDetailed = (
    <IntegrationStatusDetail
      targetState={'Published'}
      value={'Deploying'}
      currentStep={2}
      totalSteps={4}
      i18nProgressPending={'Pending'}
      i18nProgressStarting={'Starting...'}
      i18nProgressStopping={'Stopping...'}
      i18nLogUrlText={'View Log'}
    />
  );

  // Unpublishing state
  const testComponentUnpublishing = (
    <IntegrationStatusDetail
      targetState={'Unpublished'}
      i18nProgressPending={'Pending'}
      i18nProgressStarting={'Starting...'}
      i18nProgressStopping={'Stopping...'}
      i18nLogUrlText={'View Log'}
    />
  );

  it('Should show the starting state', () => {
    const { getByTestId } = render(testComponentPublishing);
    expect(getByTestId('integration-status-detail')).toHaveTextContent(
      'Starting...'
    );
  });

  it('Should show the detailed status', () => {
    const { getByTestId } = render(testComponentPublishingDetailed);
    expect(getByTestId('integration-status-detail')).toHaveTextContent('');
  });

  it('Should show the stopping state', () => {
    const { getByTestId } = render(testComponentUnpublishing);
    expect(getByTestId('integration-status-detail')).toHaveTextContent(
      'Stopping...'
    );
  });
});
