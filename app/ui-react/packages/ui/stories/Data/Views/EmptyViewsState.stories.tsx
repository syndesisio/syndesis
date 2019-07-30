import { action } from '@storybook/addon-actions';
import { text } from '@storybook/addon-knobs';
import { withKnobs } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import { EmptyViewsState } from '../../../src';

const stories = storiesOf('Data/Views/EmptyViewsState', module);
stories.addDecorator(withKnobs);

const title = 'Create View';
const info = 'There are no currently available views.  Click to create views';
const createText = 'Create View';
const createTip = 'Create a new view';
const importText = 'Import Data Source';
const importTip = 'Import views from a data source';

stories.add('Sample', () => (
  <Router>
    <EmptyViewsState
      i18nEmptyStateTitle={text('title', title)}
      i18nEmptyStateInfo={text('info', info)}
      i18nCreateView={text('createView', createText)}
      i18nCreateViewTip={text('createViewTip', createTip)}
      i18nImportViews={text('importViews', importText)}
      i18nImportViewsTip={text('importViewsTip', importTip)}
      linkCreateViewHRef={action('/data/create')}
      linkImportViewsHRef={action('/data/import')}
    />
  </Router>
));
