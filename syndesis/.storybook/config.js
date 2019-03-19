import '../../packages/.storybook/config';
import { addDecorator, configure } from '@storybook/react';
import { ApiContext } from '@syndesis/api';
import { AppContext } from '../src/app';
import { MemoryRouter as Router } from 'react-router-dom';
import * as React from 'react';
import config from '../config.storybook.json';

/* load all stories in the story folder */
const req = require.context('../stories', true, /\.stories\.tsx$/);
function loadStories() {
  req.keys().forEach(filename => req(filename));
}

const AppContextStorybook = storyFn => (
  <Router>
    <AppContext.Provider
      value={{
        config: config,
        getPodLogUrl: () => '',
        hideNavigation: () => void 0,
        logout: () => void 0,
        showNavigation: () => void 0,
      }}
    >
      <ApiContext.Provider
        value={{
          apiUri: `${config.apiBase}${config.apiEndpoint}`,
          dvApiUri: `${config.apiBase}${config.datavirt.dvUrl}`,
          headers: { 'SYNDESIS-XSRF-TOKEN': 'awesome' },
        }}
      >
        {storyFn()}
      </ApiContext.Provider>
    </AppContext.Provider>
  </Router>
);

addDecorator(AppContextStorybook);

configure(loadStories, module);
