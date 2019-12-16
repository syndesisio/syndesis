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
    props: {
      className: "syn-small-col-content"
    }
  },
  { id: 'Argle', label: 'Argle' },
  { id: 'Bargle', label: 'Bargle' },
  {
    id: 'A',
    label: 'A'
  },
  { id: 'B', label: 'B' },
  { id: 'C', label: 'C' },
  {
    id: 'HeresAReallyLongColumnName1',
    label: 'Heres a Really Long Column Name 2',
    props: {
      className: "syn-medium-col-content"
    }
  },
  {
    id: 'HeresAReallyLongColumnName1',
    label: 'Heres a Really Long Column Name 3',
    props: {
      className: "syn-medium-col-content"
    }
  },
  { id: 'LongContent2', label: 'Long Content 2' },
  { id: 'LongContent3', label: 'Long Content 3' },
];

const resultRows15 = [
  [
    {title: <div>Jeans</div>},
    {title: <div>Frissilla</div>},
    {title: <div>Italy</div>},
    {title: <div>1234 Orange Avenue, Daytona Beach, FL</div>},
    {title: <div className="syn-large-col-content">This is a column with really long content.  Like really, really, really long content.</div>},
    {title: <div>Five</div>},
    {title: <div>Six</div>},
    {title: <div>Seven</div>},
    {title: <div>Eight*</div>},
    {title: <div>Nine</div>},
    {title: <div>Ten</div>},
    {title: <div>Eleven</div>},
    {title: <div>Twelve</div>},
    {title: <div className="syn-large-col-content">This is a column with really long content.  Like really, really, really long content.</div>},
    {title: <div className="syn-large-col-content">This is a column with really long content.  Like really, really, really long content.</div>},
  ],
  [
    {title: <div>John</div>},
    {title: <div>Johnson</div>},
    {title: <div>US</div>},
    {title: <div>2345 Browns Boulevard, Cleveland, OH</div>},
    {title: <div className="syn-large-col-content">This is a column with really long content.  Like really, really, really long content.</div>},
    {title: <div>Five</div>},
    {title: <div>Six</div>},
    {title: <div>Seven</div>},
    {title: <div>Eight</div>},
    {title: <div>Nine</div>},
    {title: <div>Ten</div>},
    {title: <div>Eleven</div>},
    {title: <div>Twelve</div>},
    {title: <div className="syn-large-col-content">This is a column with really long content.  Like really, really, really long content.</div>},
    {title: <div className="syn-large-col-content">This is a column with really long content.  Like really, really, really long content.</div>},
  ],
  [
    {title: <div>Juan</div>},
    {title: <div>Bautista</div>},
    {title: <div>Brazil</div>},
    {title: <div>3456 Peach Tree Place, Atlanta, GA</div>},
    {title: <div className="syn-large-col-content">This is a column with really long content.  Like really, really, really long content.</div>},
    {title: <div>Five</div>},
    {title: <div>Six</div>},
    {title: <div>Seven</div>},
    {title: <div>Eight</div>},
    {title: <div>Nine</div>},
    {title: <div>Ten</div>},
    {title: <div>Eleven</div>},
    {title: <div>Twelve</div>},
    {title: <div className="syn-large-col-content">This is a column with really long content.  Like really, really, really long content.</div>},
    {title: <div className="syn-large-col-content">This is a column with really long content.  Like really, really, really long content.</div>},
  ],
  [
    {title: <div>Jordan</div>},
    {title: <div>Dristol</div>},
    {title: <div>Ontario</div>},
    {title: <div>4567 Surfboard Circle, San Diego, CA</div>},
    {title: <div className="syn-large-col-content">This is a column with really long content.  Like really, really, really long content.</div>},
    {title: <div>Five</div>},
    {title: <div>Six</div>},
    {title: <div>Seven</div>},
    {title: <div>Eight</div>},
    {title: <div>Nine</div>},
    {title: <div>Ten</div>},
    {title: <div>Eleven</div>},
    {title: <div>Twelve</div>},
    {title: <div className="syn-large-col-content">This is a column with really long content.  Like really, really, really long content.</div>},
    {title: <div className="syn-large-col-content">This is a column with really long content.  Like really, really, really long content.</div>},
  ],
  [
    {title: <div>Jenny</div>},
    {title: <div>Clayton</div>},
    {title: <div>US</div>},
    {title: <div>5678 Triple Crown Terrace, Louisville, KY</div>},
    {title: <div className="syn-large-col-content">This is a column with really long content.  Like really, really, really long content.</div>},
    {title: <div>Five</div>},
    {title: <div>Six</div>},
    {title: <div>Seven</div>},
    {title: <div>Eight</div>},
    {title: <div>Nine</div>},
    {title: <div>Ten</div>},
    {title: <div>Eleven</div>},
    {title: <div>Twelve</div>},
    {title: <div className="syn-large-col-content">This is a column with really long content.  Like really, really, really long content.</div>},
    {title: <div className="syn-large-col-content">This is a column with really long content.  Like really, really, really long content.</div>},
  ],
  [
    {title: <div>Jorge</div>},
    {title: <div>Rivera</div>},
    {title: <div>Mexico</div>},
    {title: <div>6789 Snakeskin Street, El Paso, TX</div>},
    {title: <div className="syn-large-col-content">This is a column with really long content.  Like really, really, really long content.</div>},
    {title: <div>Five</div>},
    {title: <div>Six</div>},
    {title: <div>Seven</div>},
    {title: <div>Eight</div>},
    {title: <div>Nine</div>},
    {title: <div>Ten</div>},
    {title: <div>Eleven</div>},
    {title: <div>Twelve</div>},
    {title: <div className="syn-large-col-content">This is a column with really long content.  Like really, really, really long content.</div>},
    {title: <div className="syn-large-col-content">This is a column with really long content.  Like really, really, really long content.</div>},
  ],
  [
    {title: <div>Jake</div>},
    {title: <div>Klein</div>},
    {title: <div>US</div>},
    {title: <div>7890 Bolo Tie Terrace, Alamogordo, NM</div>},
    {title: <div className="syn-large-col-content">This is a column with really long content.  Like really, really, really long content.</div>},
    {title: <div>Five</div>},
    {title: <div>Six</div>},
    {title: <div>Seven</div>},
    {title: <div>Eight</div>},
    {title: <div>Nine</div>},
    {title: <div>Ten</div>},
    {title: <div>Eleven</div>},
    {title: <div>Twelve</div>},
    {title: <div className="syn-large-col-content">This is a column with really long content.  Like really, really, really long content.</div>},
    {title: <div className="syn-large-col-content">This is a column with really long content.  Like really, really, really long content.</div>},
  ],
  [
    {title: <div>Julia</div>},
    {title: <div>Zhang</div>},
    {title: <div>China</div>},
    {title: <div>8901 Music City, Nashville, TN</div>},
    {title: <div className="syn-large-col-content">This is a column with really long content.  Like really, really, really long content.</div>},
    {title: <div>Five</div>},
    {title: <div>Six</div>},
    {title: <div>Seven</div>},
    {title: <div>Eight</div>},
    {title: <div>Nine</div>},
    {title: <div>Ten</div>},
    {title: <div>Eleven</div>},
    {title: <div>Twelve</div>},
    {title: <div className="syn-large-col-content">This is a column with really long content.  Like really, really, really long content.</div>},
    {title: <div className="syn-large-col-content">This is a column with really long content.  Like really, really, really long content.</div>},
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
