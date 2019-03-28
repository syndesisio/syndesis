import * as React from 'react';
import { MemoryRouter } from 'react-router';
import { render, wait } from 'react-testing-library';
import { IntegrationsPage } from './IntegrationsPage';
import { App } from '../../../app';

describe('IntegrationsPage: ', () => {
  beforeEach(async () => window.startMockServer('something'));

  afterEach(() => window.stopMockServer());

  const testComponent = (
    <MemoryRouter>
      <App>
        <IntegrationsPage />
      </App>
    </MemoryRouter>
  );

  it('Should render', async () => {
    const { getByText, queryByTestId } = render(testComponent);
    await wait(() => {
      expect(
        queryByTestId('integration-list-skeleton')
      ).not.toBeInTheDocument();
    });
    expect(getByText('test')).toBeDefined();
    expect(getByText('test2')).toBeDefined();
    expect(getByText('test3')).toBeDefined();
    expect(getByText('test 4')).toBeDefined();
  });
});
