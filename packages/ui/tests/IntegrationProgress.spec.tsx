import * as React from 'react';
import { render } from 'react-testing-library';
import { IntegrationProgress } from '../src/Integration';

export default describe('IntegrationProgress', () => {
  // Component with a log link
  const testComponent = (
    <IntegrationProgress
      value={'Deploying'}
      currentStep={2}
      totalSteps={4}
      logUrl={'http://localhost:9000/'}
      i18nLogUrlText={'View Logs'}
    />
  );

  // Component without a log link
  const testComponentNoLog = (
    <IntegrationProgress
      value={'Assembling'}
      currentStep={1}
      totalSteps={4}
      i18nLogUrlText={'View Logs'}
    />
  );

  it('Should show the progress value and steps', () => {
    const { getByTestId } = render(testComponent);
    expect(getByTestId('integration-progress-value')).toHaveTextContent(
      'Deploying ( 2 / 4 )'
    );
  });
  it('Should show the log link when supplied', () => {
    const { queryByTestId } = render(testComponent);
    expect(queryByTestId('deployment-log-link')).toBeDefined();
    expect(queryByTestId('deployment-log-link')).toHaveTextContent('View Logs');
  });
  it('Should show the progress value and steps', () => {
    const { getByTestId } = render(testComponentNoLog);
    expect(getByTestId('integration-progress-value')).toHaveTextContent(
      'Assembling ( 1 / 4 )'
    );
  });
  it('Should not show the log link if not supplied', () => {
    const { queryByTestId } = render(testComponentNoLog);
    expect(queryByTestId('deployment-log-link')).toEqual(null);
  });
});
