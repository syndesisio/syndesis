import * as React from 'react';
import * as ReactDOM from 'react-dom';
import { I18nextProvider } from 'react-i18next';
import { BrowserRouter as Router } from 'react-router-dom';

import { App } from './app';
import i18n from './i18n';
import './index.css';
import registerServiceWorker from './registerServiceWorker';

ReactDOM.render(
  <Router>
    <I18nextProvider i18n={i18n}>
      <App />
    </I18nextProvider>
  </Router>,
  document.getElementById('root') as HTMLElement
);
registerServiceWorker();
