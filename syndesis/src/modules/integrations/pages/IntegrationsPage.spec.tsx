import * as React from 'react';
import { MemoryRouter } from 'react-router';
import { render, wait } from 'react-testing-library';
import { IntegrationsPage } from './IntegrationsPage';
import { App } from '../../../app';

describe('IntegrationsPage', () => {
  beforeEach(async () => window.startMockServer('something'));

  afterEach(() => window.stopMockServer());

  const testComponent = (
    <MemoryRouter>
      <App>
        <IntegrationsPage />
      </App>
    </MemoryRouter>
  );

  it('Should fetch the data, show a loader and then render the integrations', async () => {
    const { container, debug, queryByTestId } = render(testComponent);
    await wait(() => {
      expect(
        queryByTestId('integration-list-skeleton')
      ).not.toBeInTheDocument();
    });
    expect(container).toMatchSnapshot();
  });
});
