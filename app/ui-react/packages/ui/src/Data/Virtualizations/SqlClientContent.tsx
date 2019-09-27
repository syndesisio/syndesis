import { Split, SplitItem, Stack, StackItem } from '@patternfly/react-core';
import * as H from '@syndesis/history';
import { EmptyState, OverlayTrigger, Table, Tooltip } from 'patternfly-react';
import * as React from 'react';
import { PageSection } from '../../../src/Layout';
import { GenericTable } from '../../Shared/GenericTable';
import { EmptyViewsState } from '../Virtualizations/Views/EmptyViewsState';

export interface ISqlClientContentProps {
  /**
   * The SQL selector form content
   */
  formContent: React.ReactNode;
  /**
   * ViewNames for selector
   */
  viewNames: string[];
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
  i18nEmptyStateInfo: string;
  i18nEmptyStateTitle: string;
  i18nImportViews: string;
  i18nImportViewsTip: string;
  i18nResultsTitle: string;
  i18nResultsRowCountMsg: string;
  linkCreateViewHRef: H.LocationDescriptor;
  linkImportViewsHRef: H.LocationDescriptor;
  i18nCreateViewTip?: string;
  i18nCreateView: string;
  i18nEmptyResultsTitle: string;
  i18nEmptyResultsMsg: string;
}

interface IColumn {
  id: string;
  label: string;
}

const defaultCellFormat = (value: any) => {
  // strings over 20 chars - shorten and use tooltip
  if (typeof value === "string" && value.length > 20) {
    const displayedString = `${value.substring(0,15)}...${value.substring(value.length-5)}`;
    return <OverlayTrigger
      overlay={<Tooltip id="queryResultsCellTip">{value}</Tooltip>}
      placement="top"
    >
      <Table.Heading>{displayedString}</Table.Heading>
    </OverlayTrigger>;
  }
  return <Table.Heading>{value}</Table.Heading>
};
const defaultHeaderFormat = (value: any) => <Table.Cell>{value}</Table.Cell>;

/**
 * The SQL client content.  This component includes:
 * - SqlClientForm - for selection of the view and query params
 * - GenericTable - for display of the query results
 * - EmptyStates - displayed when no views available or no results available.
 */
export const SqlClientContent: React.FunctionComponent<
  ISqlClientContentProps
> = props => {
  return (
    <PageSection>
      {props.viewNames.length > 0 ? (
        <Split gutter="md">
          <SplitItem isFilled={false}>{props.formContent}</SplitItem>
          <SplitItem isFilled={true} style={{ overflowX: 'auto' }}>
            {props.queryResultCols.length > 0 ? (
              <Stack>
                <StackItem isFilled={false}>{props.i18nResultsTitle}</StackItem>
                <StackItem isFilled={false}>
                  <small>
                    <i>
                      {props.i18nResultsRowCountMsg}
                      {props.queryResultRows.length}
                    </i>
                  </small>
                </StackItem>
                <StackItem isFilled={true}>
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
                </StackItem>
              </Stack>
            ) : (
              <EmptyState>
                <EmptyState.Title>
                  {props.i18nEmptyResultsTitle}
                </EmptyState.Title>
                <EmptyState.Info>{props.i18nEmptyResultsMsg}</EmptyState.Info>
              </EmptyState>
            )}
          </SplitItem>
        </Split>
      ) : (
        <EmptyViewsState
          i18nEmptyStateTitle={props.i18nEmptyStateTitle}
          i18nEmptyStateInfo={props.i18nEmptyStateInfo}
          i18nCreateView={props.i18nCreateView}
          i18nCreateViewTip={props.i18nCreateViewTip}
          i18nImportViews={props.i18nImportViews}
          i18nImportViewsTip={props.i18nImportViewsTip}
          linkCreateViewHRef={props.linkCreateViewHRef}
          linkImportViewsHRef={props.linkImportViewsHRef}
        />
      )}
    </PageSection>
  );
};
