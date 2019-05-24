import * as H from '@syndesis/history';
import { EmptyState, Grid, Table } from 'patternfly-react';
import * as React from 'react';
import { Container, PageSection } from '../../../src/Layout';
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
   * TargetVdb to run query
   */
  targetVdb: string;
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

const defaultCellFormat = (value: any) => (
  <Table.Heading>{value}</Table.Heading>
);
const defaultHeaderFormat = (value: any) => <Table.Cell>{value}</Table.Cell>;

/**
 * The SQL client content.  This component includes:
 * - SqlClientForm - for selection of the view and query params
 * - GenericTable - for display of the query results
 * - EmptyStates - displayed when no views available or no results available.
 */
export class SqlClientContent extends React.Component<ISqlClientContentProps> {
  public render() {
    return (
      <PageSection>
        {this.props.viewNames.length > 0 ? (
          <Grid.Row>
            <Grid.Col md={6}>
              <Container>{this.props.formContent}</Container>
            </Grid.Col>
            <Grid.Col md={6}>
              <Container>
                {this.props.queryResultRows.length > 0 ? (
                  <GenericTable
                    columns={this.props.queryResultCols.map(col => ({
                      cell: {
                        formatters: [defaultCellFormat],
                      },
                      header: {
                        formatters: [defaultHeaderFormat],
                        label: col.label,
                      },
                      property: col.id,
                    }))}
                    rows={this.props.queryResultRows}
                    rowKey={
                      this.props.queryResultCols.length > 0
                        ? this.props.queryResultCols[0].id
                        : ''
                    }
                    {...this.props}
                  />
                ) : (
                  <EmptyState>
                    <EmptyState.Title>
                      {this.props.i18nEmptyResultsTitle}
                    </EmptyState.Title>
                    <EmptyState.Info>
                      {this.props.i18nEmptyResultsMsg}
                    </EmptyState.Info>
                  </EmptyState>
                )}
              </Container>
            </Grid.Col>
          </Grid.Row>
        ) : (
          <EmptyViewsState
            i18nEmptyStateTitle={this.props.i18nEmptyStateTitle}
            i18nEmptyStateInfo={this.props.i18nEmptyStateInfo}
            i18nCreateView={this.props.i18nCreateView}
            i18nCreateViewTip={this.props.i18nCreateViewTip}
            i18nImportViews={this.props.i18nImportViews}
            i18nImportViewsTip={this.props.i18nImportViewsTip}
            linkCreateViewHRef={this.props.linkCreateViewHRef}
            linkImportViewsHRef={this.props.linkImportViewsHRef}
          />
        )}
      </PageSection>
    );
  }
}
