import { action } from '@storybook/addon-actions';
import { text } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';

import { DatabaseIcon } from '@patternfly/react-icons';
import { SelectedConnectionListView } from '../../../src';

const stories = storiesOf(
  'Data/CreateViewWizard/SelectedConnectionListView',
  module
);

const selectedConnectionIndex1 = 'SelectedConnectionListView1';
const selectedConnectionTable1 = 'contact';
const selectedConnectionName1 = 'PostgreDB';
const rows = [
  ['first_name', 'string'],
  ['last_name', 'string'],
  ['company', 'string'],
  ['create_data', 'date'],
];

stories.add('expanded list', () => (
  <Router>
    <SelectedConnectionListView
      key={selectedConnectionIndex1}
      name={selectedConnectionTable1}
      connectionIcon={<DatabaseIcon />}
      connectionName={selectedConnectionName1}
      index={0}
      toggle={action('On toggle')}
      expanded={['', 'ex-toggle0']}
      onTableRemoved={action('On table removed')}
      setShowPreviewData={action('preview the selected tables')}
      i18nRemoveSelection={text('removeSelection', 'Remove Selection')}
      i18nPreviewData={text('previewData', 'Preview Data')}
      rows={rows}
    />
  </Router>
));
