import {
  ApiContext,
  WithServerEvents,
  ServerEventsContext,
} from '@syndesis/api';
import * as React from 'react';
import { I18nextProvider } from 'react-i18next';
import { MemoryRouter } from 'react-router';
import { render, waitForElement } from 'react-testing-library';
import path from 'path';
import talkback from 'talkback';
import { AppContext } from '../../../app';
import { WithConfig } from '../../../app/WithConfig';
import i18n from '../../../i18n';
import { IntegrationsPage } from './IntegrationsPage';

const startServer = async session => {
  const server = talkback({
    host: 'http://testing',
    port: 8556,
    path: path.join(process.env.PWD!, 'tapes', session),
    record: false,
    ignoreHeaders: [
      'content-length',
      'host',
      'cookie',
      'cache-control',
      'pragma',
    ],
    silent: true,
    summary: false,
    debug: false,
  });
  await server.start();
  return server;
};

let server;

beforeEach(async () => {
  server = await startServer('something');
});

afterEach(() => {
  server.close();
});

export default describe('App', () => {
  const testComponent = (
    <MemoryRouter>
      <I18nextProvider i18n={i18n}>
        <WithConfig>
          {({ config, loading, error }) => (
            <AppContext.Provider
              value={{
                config: config!,
                getPodLogUrl: () => '',
                hideNavigation: () => void 0,
                logout: () => void 0,
                showNavigation: () => void 0,
              }}
            >
              <ApiContext.Provider
                value={{
                  apiUri: `${config!.apiBase}${config!.apiEndpoint}`,
                  dvApiUri: `${config!.apiBase}${config!.datavirt.dvUrl}`,
                  headers: { 'SYNDESIS-XSRF-TOKEN': 'awesome' },
                }}
              >
                <ApiContext.Consumer>
                  {({ apiUri, headers }) => (
                    <WithServerEvents apiUri={apiUri} headers={headers}>
                      {functions => (
                        <ServerEventsContext.Provider value={functions}>
                          <IntegrationsPage />
                        </ServerEventsContext.Provider>
                      )}
                    </WithServerEvents>
                  )}
                </ApiContext.Consumer>
              </ApiContext.Provider>
            </AppContext.Provider>
          )}
        </WithConfig>
      </I18nextProvider>
    </MemoryRouter>
  );

  it('Should render', async () => {
    const { getByText, getByTestId, queryByTestId } = render(testComponent);
    //
    // expect(getByTestId('navbar-link-/some-test-url')).toHaveAttribute(
    //   'href',
    //   '/some-test-url'
    // );
    //
    // expect(getByText('Test route')).toBeTruthy();
    //
    const content = await waitForElement(() =>
      getByTestId('test-page-content')
    );
    expect(content).toHaveTextContent('Integrations');
    //
    // expect(queryByTestId('test-unmatched-route-container')).toBeNull();
  });
});
