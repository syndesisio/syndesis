import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import { PreviewResults } from '../../../src';

const stories = storiesOf('Data/ViewEditor/PreviewResults', module);

const queryResultsTableEmptyStateInfo =
  'Click Refresh button to re-submit the query.';
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

stories.add('No results', () => (
  <Router>
    <PreviewResults
      queryResultRows={[]}
      queryResultCols={[]}
      i18nEmptyResultsTitle={queryResultsTableEmptyStateTitle}
      i18nEmptyResultsMsg={queryResultsTableEmptyStateInfo}
    />
  </Router>
));

stories.add('With results', () => (
  <Router>
    <PreviewResults
      queryResultRows={resultRows}
      queryResultCols={resultCols}
      i18nEmptyResultsTitle={queryResultsTableEmptyStateTitle}
      i18nEmptyResultsMsg={queryResultsTableEmptyStateInfo}
    />
  </Router>
));
