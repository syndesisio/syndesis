import {
  EmptyState,
  EmptyStateBody,
  EmptyStateVariant,
  Title,
} from '@patternfly/react-core';
import {
  Table,
  TableBody,
  TableHeader,
  TableVariant,
  wrappable,
  IRow
} from '@patternfly/react-table';
import { Spinner } from 'patternfly-react';
import * as React from 'react';
import { PageSection } from '../../../../src/Layout';
import './PreviewResults.css';

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
   * Array of query result rows - must match order of columns
   * Example:
   * [ ['Jean', 'Frissilla', 'Italy'],
   *   ['John', 'Johnson', 'US'],
   *   ['Juan', 'Bautista', 'Brazil'],
   *   ['Jordan', 'Dristol', 'Ontario']
   * ]
   */
  queryResultRows: Array<IRow | string[]>;
  i18nEmptyResultsTitle: string;
  i18nEmptyResultsMsg: string;
  i18nLoadingQueryResults: string;
  isLoadingPreview: boolean;
}

export interface IColumn {
  id: string;
  label: string;
  props?: {
    className?: string;
  }
}

const getColumns = (cols: IColumn[]) => {
  return cols.map(col => ({
    title: col.label,
    props: {
      className: col.props && col.props.className
    },
    transforms: [wrappable],
  }));
};

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
          {props.queryResultCols.length > 0 ? (
            <Table
              className="preview-results__tableSection"
              aria-label="Query Results Table"
              variant={TableVariant.compact}
              cells={getColumns(props.queryResultCols)}
              rows={props.queryResultRows}
            >
              <TableHeader />
              <TableBody />
            </Table>
          ) : (
            <EmptyState variant={EmptyStateVariant.full}>
              <Title headingLevel="h5" size="lg">
                {props.i18nEmptyResultsTitle}
              </Title>
              <EmptyStateBody>{props.i18nEmptyResultsMsg}</EmptyStateBody>
            </EmptyState>
          )}
        </>
      )}
    </PageSection>
  );
};
