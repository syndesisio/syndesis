import { ApiContext, ServerEventsContext } from '@syndesis/api';
import * as React from 'react';
import { I18nextProvider } from 'react-i18next';
import { MemoryRouter } from 'react-router';
import { render, wait } from 'react-testing-library';
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
      'referer',
      'origin',
      'accept',
      'accept-encoding',
      'accept-language',
      'user-agent',
      'x-forwarded-access-token',
      'x-forwarded-origin',
    ],
    silent: true,
    summary: false,
    debug: false,
    responseDecorator: (tape, req) => {
      if (tape.req.method === 'OPTIONS') {
        tape.res.status = 200;
        tape.res.body = null;
        tape.res.headers = {
          'Access-Control-Allow-Credentials': 'true',
          'Access-Control-Allow-Headers': 'syndesis-xsrf-token',
          'Access-Control-Allow-Methods':
            tape.req.headers['access-control-request-method'],
          'Access-Control-Allow-Origin': 'http://localhost',
          'Content-Length': '0',
        };
      }
      return tape;
    },
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

export default describe('IntegrationsPage', () => {
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
                <ServerEventsContext.Provider
                  value={{
                    registerChangeListener: () => void 0,
                    registerMessageListener: () => void 0,
                    unregisterChangeListener: () => void 0,
                    unregisterMessageListener: () => void 0,
                  }}
                >
                  <IntegrationsPage />
                </ServerEventsContext.Provider>
              </ApiContext.Provider>
            </AppContext.Provider>
          )}
        </WithConfig>
      </I18nextProvider>
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
