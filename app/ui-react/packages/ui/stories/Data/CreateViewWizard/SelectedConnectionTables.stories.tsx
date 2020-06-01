import { action } from '@storybook/addon-actions';
import { text } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';

import { DatabaseIcon } from '@patternfly/react-icons';
import {
  SelectedConnectionListView,
  SelectedConnectionTables,
} from '../../../src';

const stories = storiesOf(
  'Data/CreateViewWizard/SelectedConnectionTables',
  module
);

const selectedConnectionIndex1 = 'SelectedConnectionListView1';
const selectedConnectionTable1 = 'contact';
const selectedConnectionName1 = 'PostgreDB';
const selectedConnectionIndex2 = 'SelectedConnectionListView2';
const selectedConnectionTable2 = 'todo';
const selectedConnectionName2 = 'PostgreDB';

const selectedConnectionListItems = [
  <SelectedConnectionListView
    key={selectedConnectionIndex1}
    name={selectedConnectionTable1}
    connectionIcon={<DatabaseIcon />}
    connectionName={selectedConnectionName1}
    index={0}
    toggle={action('On toggle')}
    expanded={[]}
    onTableRemoved={action('On table removed')}
    setShowPreviewData={action('preview the selected tables')}
    i18nRemoveSelection={text('removeSelection', 'Remove Selection')}
    i18nPreviewData={text('previewData', 'Preview Data')}
    rows={[]}
  />,
  <SelectedConnectionListView
    key={selectedConnectionIndex2}
    name={selectedConnectionTable2}
    connectionIcon={<DatabaseIcon />}
    connectionName={selectedConnectionName2}
    index={1}
    toggle={action('On toggle')}
    expanded={[]}
    onTableRemoved={action('On table removed')}
    setShowPreviewData={action('preview the selected tables')}
    i18nRemoveSelection={text('removeSelection', 'Remove Selection')}
    i18nPreviewData={text('previewData', 'Preview Data')}
    rows={[]}
  />,
];

stories
  .add('empty list', () => (
    <Router>
      <SelectedConnectionTables
        selectedSchemaNodesLength={0}
        i18nTablesSelected={text('TablesSelected', 'Tables Seected')}
        i18nEmptyTablePreview={text(
          'EmptyTablePreview',
          'Please select one or more tables for your view. If no table is selected, a default view will be generated.'
        )}
        i18nEmptyTablePreviewTitle={text(
          'EmptyTablePreviewTitle',
          'No tables selected'
        )}
        children={[]}
      />
    </Router>
  ))
  .add('With list', () => (
    <Router>
      <SelectedConnectionTables
        selectedSchemaNodesLength={selectedConnectionListItems.length}
        i18nTablesSelected={text('TablesSelected', 'Tables Seected')}
        i18nEmptyTablePreview={text(
          'EmptyTablePreview',
          'Please select one or more tables for your view. If no table is selected, a default view will be generated.'
        )}
        i18nEmptyTablePreviewTitle={text(
          'EmptyTablePreviewTitle',
          'No tables selected'
        )}
        children={selectedConnectionListItems}
      />
    </Router>
  ));
