import * as React from 'react';
import 'jest-dom/extend-expect';
import { cleanup, render } from 'react-testing-library';
import { IntegrationStatus } from '../../src/Integration';

export default describe('IntegrationStatus', () => {
  const props = {
    i18nPublished: 'Running',
    i18nUnpublished: 'Stopped',
    i18nError: 'Error',
  };

  afterEach(cleanup);

  it('should display Running for a Published integration status', () => {
    const { getByTestId } = render(
      <IntegrationStatus {...props} currentState={'Published'} />
    );

    expect(getByTestId('integration-status-status-label')).toHaveTextContent(
      'Running'
    );
  });

  it('should display Stopped for an Unpublished integration status', () => {
    const { getByTestId } = render(
      <IntegrationStatus {...props} currentState={'Unpublished'} />
    );

    expect(getByTestId('integration-status-status-label')).toHaveTextContent(
      'Stopped'
    );
  });

  it('should display Error for an Error integration status', () => {
    const { getByTestId } = render(
      <IntegrationStatus {...props} currentState={'Error'} />
    );

    expect(getByTestId('integration-status-status-label')).toHaveTextContent(
      'Error'
    );
  });
});
