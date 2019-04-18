import { WithVirtualizationHelpers } from '@syndesis/api';
import { AutoForm, IFormDefinition } from '@syndesis/auto-form';
import { QueryResults, ViewDefinition } from '@syndesis/models';
import { Container } from '@syndesis/ui';
// tslint:disable-next-line:no-implicit-dependencies
import { DropdownButton, MenuItem } from 'patternfly-react';
import * as React from 'react';
import i18n from '../../../i18n';
import { getPreviewSql } from './VirtualizationUtils';

export interface IViewSqlFormProps {
  /**
   * @param views the array of view definitions for the current virtualization
   */
  views: ViewDefinition[];

  /**
   * @param targetVdb the name of the vdb to query
   */
  targetVdb: string;

  /**
   * @param onQueryResultsChanged the parameter function called by the parent component to that returns
   * the promotes the value of the latest query results
   */
  onQueryResultsChanged: (queryResults: QueryResults) => void;
}

export interface IViewSqlFormState {
  queryResults: QueryResults;
  querySql: string;
  selectedView?: ViewDefinition;
}

export class ViewSqlForm extends React.Component<
  IViewSqlFormProps,
  IViewSqlFormState
> {
  public static queryResultsEmpty: QueryResults = {
    columns: [],
    rows: [],
  };

  public constructor(props: IViewSqlFormProps) {
    super(props);
    this.state = {
      queryResults: ViewSqlForm.queryResultsEmpty,
      querySql: '',
    };

    this.updateQueryResults = this.updateQueryResults.bind(this);
    this.viewSelected = this.viewSelected.bind(this);
  }

  public updateQueryResults(results: QueryResults) {
    {
      results && results.columns.length > 0
        ? this.setState({ queryResults: results })
        : this.setState({ queryResults: ViewSqlForm.queryResultsEmpty });
    }

    this.props.onQueryResultsChanged(this.state.queryResults);
  }

  public viewSelected() {
    const viewSql = getPreviewSql(this.props.views[0]);
    // TODO: change the defaultValue in the SqlStatement text area
    // to reflect the newly selected view name (if changed)
    // Maybe even show a text field showing the selected component depending on the PF4 widget type
    this.setState({
      queryResults: ViewSqlForm.queryResultsEmpty,
      querySql: viewSql,
      selectedView: this.props.views[0],
    });
  }

  public render() {
    const formDefinition = {
      rowLimit: {
        componentProperty: true,
        defaultValue: '10',
        deprecated: false,
        description: i18n.t(
          'data:virtualization.viewSqlFormRowLimitDescription'
        ),
        displayName: i18n.t('data:virtualization.viewSqlFormRowLimit'),
        javaType: 'java.lang.Integer',
        kind: 'property',
        order: 3,
        required: true,
        secret: false,
        type: 'number',
      },
      rowOffset: {
        componentProperty: true,
        defaultValue: '1',
        deprecated: false,
        description: i18n.t(
          'data:virtualization.viewSqlFormRowOffsetDescription'
        ),
        displayName: i18n.t('data:virtualization.viewSqlFormRowOffset'),
        javaType: 'java.lang.Integer',
        kind: 'property',
        order: 4,
        required: false,
        secret: false,
        type: 'number',
      },
      sqlStatement: {
        componentProperty: true,
        defaultValue: this.state.querySql,
        deprecated: false,
        description: i18n.t(
          'data:virtualization.viewSqlFormSqlStatementDescription'
        ),
        displayName: i18n.t('data:virtualization.viewSqlFormSqlStatement'),
        height: 300,
        javaType: 'java.lang.String',
        kind: 'property',
        order: 2,
        required: true,
        secret: false,
        type: 'textarea',
      },
    } as IFormDefinition;

    return (
      // TODO need to retrieve real user here
      <WithVirtualizationHelpers username="developer">
        {({ queryVirtualization }) => {
          const doSubmit = async (value: any) => {
            const results: QueryResults = await queryVirtualization(
              this.props.targetVdb,
              value.sqlStatement,
              value.rowLimit,
              value.rowOffset
            );
            this.updateQueryResults(results);
          };
          return (
            // NOTE: This is a fake SELECTOR widget (i.e. dropdown menu) just to show a selector-like UI component
            // TODO replace DropdownButton with
            <>
              <Container>
                <DropdownButton
                  bsStyle="default"
                  title={
                    // TODO: i18n translations
                    this.props.views.length > 0
                      ? 'Select View...'
                      : 'No Views Defined'
                  }
                  id="dropdown-example"
                  onClick={this.viewSelected}
                >
                  {this.props.views.map((view, index) => (
                    <MenuItem key={index}>{view.viewName}</MenuItem>
                  ))}
                </DropdownButton>
              </Container>
              <Container>
                <AutoForm
                  definition={formDefinition}
                  initialValue={''}
                  i18nRequiredProperty={'*Required'}
                  onSave={doSubmit}
                >
                  {({ fields, handleSubmit }) => (
                    <React.Fragment>
                      {fields}
                      <button
                        type="button"
                        className="btn btn-primary"
                        onClick={handleSubmit}
                      >
                        Submit
                      </button>
                    </React.Fragment>
                  )}
                </AutoForm>
              </Container>
            </>
          );
        }}
      </WithVirtualizationHelpers>
    );
  }
}
