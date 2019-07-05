import * as React from 'react';
import { render } from 'react-testing-library';
import { ProgressWithLink } from '../src/Shared';

export default describe('IntegrationProgress', () => {
  // Component with a log link
  const testComponent = (
    <ProgressWithLink
      value={'Deploying'}
      currentStep={2}
      totalSteps={4}
      logUrl={'http://localhost:9000/'}
      i18nLogUrlText={'View Logs'}
    />
  );

  // Component without a log link
  const testComponentNoLog = (
    <ProgressWithLink
      value={'Assembling'}
      currentStep={1}
      totalSteps={4}
      i18nLogUrlText={'View Logs'}
    />
  );

  it('Should show the progress value and steps', () => {
    const { getByTestId } = render(testComponent);
    expect(getByTestId('progress-with-link-value')).toHaveTextContent(
      'Deploying ( 2 / 4 )'
    );
  });
  it('Should show the log link when supplied', () => {
    const { queryByTestId } = render(testComponent);
    expect(queryByTestId('progress-with-link-log-url')).toBeDefined();
    expect(queryByTestId('progress-with-link-log-url')).toHaveTextContent(
      'View Logs'
    );
  });
  it('Should show the progress value and steps', () => {
    const { getByTestId } = render(testComponentNoLog);
    expect(getByTestId('progress-with-link-value')).toHaveTextContent(
      'Assembling ( 1 / 4 )'
    );
  });
  it('Should not show the log link if not supplied', () => {
    const { queryByTestId } = render(testComponentNoLog);
    expect(queryByTestId('progress-with-link-log-url')).toEqual(null);
  });
});
