import { PageSection } from '@patternfly/react-core';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { PreviewResults } from '../../../src';

const stories = storiesOf('Data/ViewEditor/PreviewResults', module);

const queryResultsTableEmptyStateInfo =
  'Click Refresh button to re-submit the query.';
const queryResultsTableEmptyStateTitle = 'NO DATA AVAILABLE';

const resultCols = [
  { id: 'FirstName', label: 'First Name' },
  { id: 'LastName', label: 'Last Name' },
  { id: 'Country', label: 'Country' },
  { id: 'Address', label: 'Address' },
];

const resultRows = [
  {
    FirstName: 'Jean',
    LastName: 'Frissilla',
    Country: 'Italy',
    Address: '1234 Orange Avenue, Daytona Beach, FL',
  },
  {
    FirstName: 'John',
    LastName: 'Johnson',
    Country: 'US',
    Address: '2345 Browns Boulevard, Cleveland, OH',
  },
  {
    FirstName: 'Juan',
    LastName: 'Bautista',
    Country: 'Brazil',
    Address: '3456 Peach Tree Place, Atlanta, GA',
  },
  {
    FirstName: 'Jordan',
    LastName: 'Dristol',
    Country: 'Ontario',
    Address: '4567 Surfboard Circle, San Diego, CA',
  },
  {
    FirstName: 'Jenny',
    LastName: 'Clayton',
    Country: 'US',
    Address: '5678 Triple Crown Terrace, Louisville, KY',
  },
  {
    FirstName: 'Jorge',
    LastName: 'Rivera',
    Country: 'Mexico',
    Address: '6789 Snakeskin Street, El Paso, TX',
  },
  {
    FirstName: 'Jake',
    LastName: 'Klein',
    Country: 'US',
    Address: '7890 Bolo Tie Terrace, Alamogordo, NM',
  },
  {
    FirstName: 'Julia',
    LastName: 'Zhang',
    Country: 'China',
    Address: '8901 Music City, Nashville, TN',
  },
];

stories.add('No results', () => (
  <PreviewResults
    queryResultRows={[]}
    queryResultCols={[]}
    i18nEmptyResultsTitle={queryResultsTableEmptyStateTitle}
    i18nEmptyResultsMsg={queryResultsTableEmptyStateInfo}
    i18nLoadingQueryResults={'Loading'}
    isLoadingPreview={false}
  />
));

stories.add(
  'With results',
  () => (
    <PageSection>
      <PreviewResults
        queryResultRows={resultRows}
        queryResultCols={resultCols}
        i18nEmptyResultsTitle={queryResultsTableEmptyStateTitle}
        i18nEmptyResultsMsg={queryResultsTableEmptyStateInfo}
        i18nLoadingQueryResults={'Loading'}
        isLoadingPreview={false}
      />
    </PageSection>
  ),
  { notes: 'Resize window and make sure table can scroll horizontally' }
);

stories.add('Loading', () => (
  <PreviewResults
    queryResultRows={[]}
    queryResultCols={[]}
    i18nEmptyResultsTitle={queryResultsTableEmptyStateTitle}
    i18nEmptyResultsMsg={queryResultsTableEmptyStateInfo}
    i18nLoadingQueryResults={'Loading'}
    isLoadingPreview={true}
  />
));
