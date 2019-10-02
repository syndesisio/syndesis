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

const resultCols15 = [
  { id: 'FirstName', label: 'First Name' },
  { id: 'LastName', label: 'Last Name' },
  { id: 'Country', label: 'Country' },
  { id: 'Address', label: 'Address' },
  { id: 'LongContent1', label: 'Long Content 1' },
  {
    id: 'HeresAReallyLongColumnName1',
    label: 'Heres a Really Long Column Name 1',
  },
  { id: 'Argle', label: 'Argle' },
  { id: 'Bargle', label: 'Bargle' },
  { id: 'A', label: 'A' },
  { id: 'B', label: 'B' },
  { id: 'C', label: 'C' },
  {
    id: 'HeresAReallyLongColumnName1',
    label: 'Heres a Really Long Column Name 2',
  },
  {
    id: 'HeresAReallyLongColumnName1',
    label: 'Heres a Really Long Column Name 3',
  },
  { id: 'LongContent2', label: 'Long Content 2' },
  { id: 'LongContent3', label: 'Long Content 3' },
];

const resultRows15 = [
  [
    'Jean',
    'Frissilla',
    'Italy',
    '1234 Orange Avenue, Daytona Beach, FL',
    'This is a column with really long content.  Like really, really, really long content.',
    'Five',
    'Six',
    'Seven',
    'Eight',
    'Nine',
    'Ten',
    'Eleven',
    'Twelve',
    'This is a column with really long content.  Like really, really, really long content.',
    'This is a column with really long content.  Like really, really, really long content.',
  ],
  [
    'John',
    'Johnson',
    'US',
    '2345 Browns Boulevard, Cleveland, OH',
    'This is a column with really long content.  Like really, really, really long content.',
    'Five',
    'Six',
    'Seven',
    'Eight',
    'Nine',
    'Ten',
    'Eleven',
    'Twelve',
    'This is a column with really long content.  Like really, really, really long content.',
    'This is a column with really long content.  Like really, really, really long content.',
  ],
  [
    'Juan',
    'Bautista',
    'Brazil',
    '3456 Peach Tree Place, Atlanta, GA',
    'This is a column with really long content.  Like really, really, really long content.',
    'Five',
    'Six',
    'Seven',
    'Eight',
    'Nine',
    'Ten',
    'Eleven',
    'Twelve',
    'This is a column with really long content.  Like really, really, really long content.',
    'This is a column with really long content.  Like really, really, really long content.',
  ],
  [
    'Jordan',
    'Dristol',
    'Ontario',
    '4567 Surfboard Circle, San Diego, CA',
    'This is a column with really long content.  Like really, really, really long content.',
    'Five',
    'Six',
    'Seven',
    'Eight',
    'Nine',
    'Ten',
    'Eleven',
    'Twelve',
    'This is a column with really long content.  Like really, really, really long content.',
    'This is a column with really long content.  Like really, really, really long content.',
  ],
  [
    'Jenny',
    'Clayton',
    'US',
    '5678 Triple Crown Terrace, Louisville, KY',
    'This is a column with really long content.  Like really, really, really long content.',
    'Five',
    'Six',
    'Seven',
    'Eight',
    'Nine',
    'Ten',
    'Eleven',
    'Twelve',
    'This is a column with really long content.  Like really, really, really long content.',
    'This is a column with really long content.  Like really, really, really long content.',
  ],
  [
    'Jorge',
    'Rivera',
    'Mexico',
    '6789 Snakeskin Street, El Paso, TX',
    'This is a column with really long content.  Like really, really, really long content.',
    'Five',
    'Six',
    'Seven',
    'Eight',
    'Nine',
    'Ten',
    'Eleven',
    'Twelve',
    'This is a column with really long content.  Like really, really, really long content.',
    'This is a column with really long content.  Like really, really, really long content.',
  ],
  [
    'Jake',
    'Klein',
    'US',
    '7890 Bolo Tie Terrace, Alamogordo, NM',
    'This is a column with really long content.  Like really, really, really long content.',
    'Five',
    'Six',
    'Seven',
    'Eight',
    'Nine',
    'Ten',
    'Eleven',
    'Twelve',
    'This is a column with really long content.  Like really, really, really long content.',
    'This is a column with really long content.  Like really, really, really long content.',
  ],
  [
    'Julia',
    'Zhang',
    'China',
    '8901 Music City, Nashville, TN',
    'This is a column with really long content.  Like really, really, really long content.',
    'Five',
    'Six',
    'Seven',
    'Eight',
    'Nine',
    'Ten',
    'Eleven',
    'Twelve',
    'This is a column with really long content.  Like really, really, really long content.',
    'This is a column with really long content.  Like really, really, really long content.',
  ],
];

const resultRows = [
  ['Jean', 'Frissilla', 'Italy', '1234 Orange Avenue, Daytona Beach, FL'],
  ['John', 'Johnson', 'US', '2345 Browns Boulevard, Cleveland, OH'],
  ['Juan', 'Bautista', 'Brazil', '3456 Peach Tree Place, Atlanta, GA'],
  ['Jordan', 'Dristol', 'Ontario', '4567 Surfboard Circle, San Diego, CA'],
  ['Jenny', 'Clayton', 'US', '5678 Triple Crown Terrace, Louisville, KY'],
  ['Jorge', 'Rivera', 'Mexico', '6789 Snakeskin Street, El Paso, TX'],
  ['Jake', 'Klein', 'US', '7890 Bolo Tie Terrace, Alamogordo, NM'],
  ['Julia', 'Zhang', 'China', '8901 Music City, Nashville, TN'],
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
  'With results - 4 cols',
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

stories.add(
  'With results - 15 cols',
  () => (
    <PageSection>
      <PreviewResults
        queryResultRows={resultRows15}
        queryResultCols={resultCols15}
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
