import { EmptyState, EmptyStateBody, Title } from '@patternfly/react-core';
import { Spinner, Table } from 'patternfly-react';
import * as React from 'react';
import { PageSection } from '../../../../src/Layout';
import { GenericTable } from '../../../Shared/GenericTable';

export interface IPreviewResultsProps {
  /**
   * Array of column info for the query results.  (The column id and display label)
   * Example:
   * [ { id: 'fName', label: 'First Name'},
   *   { id: 'lName', label: 'Last Name'},
   *   { id: 'country', label: 'Country' }
   * ]
   */
  queryResultCols: IColumn[];
  /**
   * Array of query result rows - must match up with column ids
   * Example:
   * [ { fName: 'Jean', lName: 'Frissilla', country: 'Italy' },
   *   { fName: 'John', lName: 'Johnson', country: 'US' },
   *   { fName: 'Juan', lName: 'Bautista', country: 'Brazil' },
   *   { fName: 'Jordan', lName: 'Dristol', country: 'Ontario' }
   * ]
   */
  queryResultRows: Array<{}>;
  i18nEmptyResultsTitle: string;
  i18nEmptyResultsMsg: string;
  i18nLoadingQueryResults: string;
  isLoadingPreview: boolean;
}

export interface IColumn {
  id: string;
  label: string;
}

const defaultCellFormat = (value: any) => (
  <Table.Heading>{value}</Table.Heading>
);
const defaultHeaderFormat = (value: any) => <Table.Cell>{value}</Table.Cell>;

/**
 * The PreviewResults component.  Displays table of supplied results.
 */
export const PreviewResults: React.FunctionComponent<
  IPreviewResultsProps
> = props => {

  return (
    <PageSection>
      {props.isLoadingPreview ? (
        <>
        <Spinner loading={true} inline={true} />
        {props.i18nLoadingQueryResults}
        </>
        ) : (
          <>
            { props.queryResultCols.length > 0 ? (
              <div className="generic-table_content">
                <GenericTable
                  columns={props.queryResultCols.map(col => ({
                    cell: {
                      formatters: [defaultCellFormat],
                    },
                    header: {
                      formatters: [defaultHeaderFormat],
                      label: col.label,
                    },
                    property: col.id,
                  }))}
                  rows={props.queryResultRows}
                  rowKey={
                    props.queryResultCols.length > 0
                      ? props.queryResultCols[0].id
                      : ''
                  }
                  {...props}
                />
              </div>
            ) : (
                <EmptyState>
                  <Title headingLevel="h5" size="lg">
                    {props.i18nEmptyResultsTitle}
                  </Title>
                  <EmptyStateBody>
                    {props.i18nEmptyResultsMsg}
                  </EmptyStateBody>
                </EmptyState>
              )}
          </>
        )}
    </PageSection>
  );

}
