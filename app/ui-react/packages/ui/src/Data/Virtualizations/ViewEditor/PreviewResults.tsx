import {
  EmptyState,
  EmptyStateBody,
  EmptyStateVariant,
  Spinner,
  Title,
} from '@patternfly/react-core';
import * as React from 'react';
import { PageSection } from '../../../../src/Layout';
import { SqlResultsTable } from '../SqlResultsTable';
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
  queryResultRows: string[][];
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
  };
}

/**
 * The PreviewResults component.  Displays table of supplied results.
 */
export const PreviewResults: React.FunctionComponent<IPreviewResultsProps> = props => {
  return (
    <PageSection>
      {props.isLoadingPreview ? (
        <>
          <Spinner size={'lg'} />
          {props.i18nLoadingQueryResults}
        </>
      ) : (
        <>
          {props.queryResultCols.length > 0 ? (
            <div className={'preview-results__tableSection'}>
              <SqlResultsTable
                queryResultCols={props.queryResultCols}
                queryResultRows={props.queryResultRows}
              />
            </div>
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
