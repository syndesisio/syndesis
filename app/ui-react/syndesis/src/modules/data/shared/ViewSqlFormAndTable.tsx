import { QueryResults } from '@syndesis/models';
import { Container, GenericTable } from '@syndesis/ui';
// tslint:disable-next-line:no-implicit-dependencies
import { EmptyState, Grid, Table } from 'patternfly-react';
import * as React from 'react';
import i18n from '../../../i18n';
import { ViewSqlForm } from '../shared/';

export interface IViewSqlFormAndTableProps {
  /**
   * @param viewNames the list of view names for a virtualization
   * This will come from the virtualization.serviceViewDefinitions string[] array
   */
  viewNames: string[];

  /**
   * @param virtualizationName the name of the virtualization
   */
  virtualizationName: string;
}

export interface IViewSqlFormAndTableState {
  queryResults: QueryResults;
}

export class ViewSqlFormAndTable extends React.Component<
  IViewSqlFormAndTableProps,
  IViewSqlFormAndTableState
> {
  public constructor(props: IViewSqlFormAndTableProps) {
    super(props);
    this.state = {
      queryResults: ViewSqlForm.queryResultsEmpty,
    };

    this.setQueryResults = this.setQueryResults.bind(this);
  }

  public setQueryResults(results: QueryResults) {
    {
      results && results.columns.length > 0
        ? this.setState({
            queryResults: results,
          })
        : this.setState({
            queryResults: ViewSqlForm.queryResultsEmpty,
          });
    }
  }

  public render() {
    const defaultCellFormat = (value: any) => (
      <Table.Heading>{value}</Table.Heading>
    );
    const defaultHeaderFormat = (value: any) => (
      <Table.Cell>{value}</Table.Cell>
    );

    return (
      <Grid.Row>
        <Grid.Col md={6}>
          <Container>
            <ViewSqlForm
              viewNames={this.props.viewNames}
              virtualizationName={this.props.virtualizationName}
              onQueryResultsChanged={this.setQueryResults}
            />
          </Container>
        </Grid.Col>
        <Grid.Col md={6}>
          <Container>
            {this.state.queryResults.rows.length > 0 ? (
              <GenericTable
                columns={this.state.queryResults.columns.map(c => ({
                  cell: {
                    formatters: [defaultCellFormat],
                  },
                  header: {
                    formatters: [defaultHeaderFormat],
                    label: c.label,
                  },
                  property: c.name,
                }))}
                rows={this.state.queryResults.rows
                  .map(row => row.row)
                  .map(row =>
                    row.reduce(
                      // tslint:disable-next-line:no-shadowed-variable
                      (row, r, idx) => ({
                        ...row,
                        [this.state.queryResults.columns[idx].name]: r,
                      }),
                      {}
                    )
                  )}
                rowKey={
                  this.state.queryResults.columns.length > 0
                    ? this.state.queryResults.columns[0].name
                    : ''
                }
                {...this.props}
              />
            ) : (
              <EmptyState>
                <EmptyState.Title>
                  {i18n.t(
                    'data:virtualization.queryResultsTableEmptyStateTitle'
                  )}
                </EmptyState.Title>
                <EmptyState.Info>
                  {i18n.t(
                    'data:virtualization.queryResultsTableEmptyStateInfo'
                  )}
                </EmptyState.Info>
              </EmptyState>
            )}
          </Container>
        </Grid.Col>
      </Grid.Row>
    );
  }
}
