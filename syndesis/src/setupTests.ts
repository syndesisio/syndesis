// this adds jest-dom's custom assertions
import 'jest-dom/extend-expect';

// react-testing-library renders your components to document.body,
// this will ensure they're removed after each test.
import 'react-testing-library/cleanup-after-each';

import EventSource from 'eventsourcemock';

// replace some components with their mock
jest.mock('./app/WithConfig');
jest.mock('./containers/PageTitle');

const originalFetch = window.fetch;
window.fetch = (url: string, options: RequestInit) =>
  originalFetch(url, options);

Object.defineProperty(window, 'EventSource', {
  value: EventSource,
});
