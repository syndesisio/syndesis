import EventSource from 'eventsourcemock';
import path from 'path';
import talkback from 'talkback';

// this adds jest-dom's custom assertions
import 'jest-dom/extend-expect';

// react-testing-library renders your components to document.body,
// this will ensure they're removed after each test.
import 'react-testing-library/cleanup-after-each';

declare global {
  interface Window {
    startMockServer(session: string): Promise<void>;
    stopMockServer(): void;
  }
}

// replace some components with their mock
jest.mock('./app/WithConfig');
jest.mock('./app/App');
jest.mock('./containers/PageTitle');

const originalFetch = window.fetch;
window.fetch = (url: string, options: RequestInit) =>
  originalFetch(url, options);

Object.defineProperty(window, 'EventSource', {
  value: EventSource,
});

let mockServer;
Object.defineProperty(window, 'startMockServer', {
  value: async session => {
    window.stopMockServer();
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
    mockServer = server;
  },
});

Object.defineProperty(window, 'stopMockServer', {
  value: () => {
    if (mockServer) {
      mockServer.close();
      mockServer = undefined;
    }
  },
});
