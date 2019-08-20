import { withKnobs } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import { Container, SqlClientContent } from '../../../src';

const stories = storiesOf('Data/Virtualizations/SqlClientContent', module);
stories.addDecorator(withKnobs);

const viewNames = ['view1', 'view2', 'view3'];
const queryResultsTitle = 'Query Results';
const queryResultRowCountMsg = 'Number of Rows: ';
const emptyStateTitle = 'Create View';
const emptyStateInfo =
  'There are no views available. Please click one of the buttons below to create views.';
const createText = 'Create a View';
const createTip = 'Create a new View for this virtualization';
const importText = 'Import Data Source';
const importTip = 'Import views from a data source';
const queryResultsTableEmptyStateInfo =
  'Query has not yet been executed.\nSelect view, enter SQL query and click Submit';
const queryResultsTableEmptyStateTitle = 'NO DATA AVAILABLE';

const resultCols = [
  { id: 'FirstName', label: 'First Name' },
  { id: 'LastName', label: 'Last Name' },
  { id: 'Country', label: 'Country' },
];

const resultRows = [
  { FirstName: 'Jean', LastName: 'Frissilla', Country: 'Italy' },
  { FirstName: 'John', LastName: 'Johnson', Country: 'US' },
  { FirstName: 'Juan', LastName: 'Bautista', Country: 'Brazil' },
  { FirstName: 'Jordan', LastName: 'Dristol', Country: 'Ontario' },
  { FirstName: 'Jenny', LastName: 'Clayton', Country: 'US' },
  { FirstName: 'Jorge', LastName: 'Rivera', Country: 'Mexico' },
  { FirstName: 'Jake', LastName: 'Klein', Country: 'US' },
  { FirstName: 'Julia', LastName: 'Zhang', Country: 'China' },
];

stories.add('No Views', () => (
  <Router>
    <SqlClientContent
      formContent={
        <Container>
          <h2>Form Content goes here</h2>
        </Container>
      }
      viewNames={[]}
      queryResultRows={[]}
      queryResultCols={[]}
      i18nResultsTitle={queryResultsTitle}
      i18nResultsRowCountMsg={queryResultRowCountMsg}
      i18nEmptyStateInfo={emptyStateInfo}
      i18nEmptyStateTitle={emptyStateTitle}
      i18nImportViews={importText}
      i18nImportViewsTip={importTip}
      i18nCreateView={createText}
      i18nCreateViewTip={createTip}
      linkCreateViewHRef={''}
      linkImportViewsHRef={''}
      i18nEmptyResultsTitle={queryResultsTableEmptyStateTitle}
      i18nEmptyResultsMsg={queryResultsTableEmptyStateInfo}
    />
  </Router>
));

stories.add('With Views, no query results', () => (
  <Router>
    <SqlClientContent
      formContent={
        <Container>
          <h2>Form Content goes here</h2>
        </Container>
      }
      viewNames={viewNames}
      queryResultRows={[]}
      queryResultCols={[]}
      i18nResultsTitle={queryResultsTitle}
      i18nResultsRowCountMsg={queryResultRowCountMsg}
      i18nEmptyStateInfo={emptyStateInfo}
      i18nEmptyStateTitle={emptyStateTitle}
      i18nImportViews={importText}
      i18nImportViewsTip={importTip}
      i18nCreateView={createText}
      i18nCreateViewTip={createTip}
      linkCreateViewHRef={''}
      linkImportViewsHRef={''}
      i18nEmptyResultsTitle={queryResultsTableEmptyStateTitle}
      i18nEmptyResultsMsg={queryResultsTableEmptyStateInfo}
    />
  </Router>
));

stories.add('With Views, with query results', () => (
  <Router>
    <SqlClientContent
      formContent={
        <Container>
          <h2>Form Content goes here</h2>
        </Container>
      }
      viewNames={viewNames}
      queryResultRows={resultRows}
      queryResultCols={resultCols}
      i18nResultsTitle={queryResultsTitle}
      i18nResultsRowCountMsg={queryResultRowCountMsg}
      i18nEmptyStateInfo={emptyStateInfo}
      i18nEmptyStateTitle={emptyStateTitle}
      i18nImportViews={importText}
      i18nImportViewsTip={importTip}
      i18nCreateView={createText}
      i18nCreateViewTip={createTip}
      linkCreateViewHRef={''}
      linkImportViewsHRef={''}
      i18nEmptyResultsTitle={queryResultsTableEmptyStateTitle}
      i18nEmptyResultsMsg={queryResultsTableEmptyStateInfo}
    />
  </Router>
));
