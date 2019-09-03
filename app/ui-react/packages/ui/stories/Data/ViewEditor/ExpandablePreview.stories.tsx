import { action } from '@storybook/addon-actions';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { ExpandablePreview, PreviewButtonSelection } from '../../../src';

const stories = storiesOf('Data/ViewEditor/ExpandablePreview', module);

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

const viewDdl =
  'CREATE VIEW PgCustomer_account (\n\tRowId long,\n\taccount_id integer,\n\tssn string,\n\tstatus string,\n\ttype string,\n\tdateopened timestamp,\n\tdateclosed timestamp,\n\tPRIMARY KEY(RowId)\n)\nAS\nSELECT ROW_NUMBER() OVER (ORDER BY account_id), account_id, ssn, status, type, dateopened, dateclosed FROM pgcustomerschemamodel.account;';

stories.add('collapsed', () => {
  return (
    <ExpandablePreview
      i18nEmptyResultsTitle={queryResultsTableEmptyStateTitle}
      i18nEmptyResultsMsg={queryResultsTableEmptyStateInfo}
      i18nHidePreview={'Hide Preview'}
      i18nShowPreview={'Show Preview'}
      // i18nSelectSqlText={'SQL'}
      // i18nSelectPreviewText={'Preview'}
      initialExpanded={false}
      onPreviewExpandedChanged={action('expanded changed')}
      // onPreviewButtonSelectionChanged={action('selection changed')}
      onRefreshResults={action('refresh results')}
      queryResultCols={resultCols}
      queryResultRows={resultRows}
      viewDdl={viewDdl}
    />
  );
})

.add('expanded, Preview with results', () => {
  return (
    <ExpandablePreview
      i18nEmptyResultsTitle={queryResultsTableEmptyStateTitle}
      i18nEmptyResultsMsg={queryResultsTableEmptyStateInfo}
      i18nHidePreview={'Hide Preview'}
      i18nShowPreview={'Show Preview'}
      // i18nSelectSqlText={'SQL'}
      // i18nSelectPreviewText={'Preview'}
      initialExpanded={true}
      // initialPreviewButtonSelection={PreviewButtonSelection.PREVIEW}
      onPreviewExpandedChanged={action('expanded changed')}
      // onPreviewButtonSelectionChanged={action('selection changed')}
      onRefreshResults={action('refresh results')}
      queryResultCols={resultCols}
      queryResultRows={resultRows}
      viewDdl={viewDdl}
    />
  );
})

.add('expanded, Preview no results', () => {
  return (
    <ExpandablePreview
      i18nEmptyResultsTitle={queryResultsTableEmptyStateTitle}
      i18nEmptyResultsMsg={queryResultsTableEmptyStateInfo}
      i18nHidePreview={'Hide Preview'}
      i18nShowPreview={'Show Preview'}
      // i18nSelectSqlText={'SQL'}
      // i18nSelectPreviewText={'Preview'}
      initialExpanded={true}
      // initialPreviewButtonSelection={PreviewButtonSelection.PREVIEW}
      onPreviewExpandedChanged={action('expanded changed')}
      // onPreviewButtonSelectionChanged={action('selection changed')}
      onRefreshResults={action('refresh results')}
      queryResultCols={[]}
      queryResultRows={[]}
      viewDdl={viewDdl}
    />
  );
});

// .add('expanded, SQL', () => {
//   return (
//     <ExpandablePreview
//       i18nEmptyResultsTitle={queryResultsTableEmptyStateTitle}
//       i18nEmptyResultsMsg={queryResultsTableEmptyStateInfo}
//       i18nHidePreview={'Hide Preview'}
//       i18nShowPreview={'Show Preview'}
//       i18nSelectSqlText={'SQL'}
//       i18nSelectPreviewText={'Preview'}
//       initialExpanded={true}
//       initialPreviewButtonSelection={PreviewButtonSelection.SQL}
//       onPreviewExpandedChanged={action('expanded changed')}
//       onPreviewButtonSelectionChanged={action('selection changed')}
//       onRefreshResults={action('refresh results')}
//       queryResultCols={[]}
//       queryResultRows={[]}
//       viewDdl={viewDdl}
//     />
//   );
// });
