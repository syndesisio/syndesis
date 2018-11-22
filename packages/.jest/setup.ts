// this adds jest-dom's custom assertions
import 'jest-dom/extend-expect';

// react-testing-library renders your components to document.body,
// this will ensure they're removed after each test.
import 'react-testing-library/cleanup-after-each';

import MessageChannel from './mocks/MessageChannel';
import MessagePort from './mocks/MessagePort';

(window as any).MessageChannel = MessageChannel;
(window as any).MessagePort = MessagePort;
