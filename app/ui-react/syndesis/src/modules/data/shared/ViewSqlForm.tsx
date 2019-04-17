import { WithVirtualizationHelpers } from '@syndesis/api';
import { AutoForm, IFormDefinition } from '@syndesis/auto-form';
import { QueryResults } from '@syndesis/models';
import { Container } from '@syndesis/ui';
// tslint:disable-next-line:no-implicit-dependencies
import { DropdownButton, MenuItem } from 'patternfly-react';
import * as React from 'react';
import i18n from '../../../i18n';

export interface IViewSqlFormProps {
  /**
   * @param viewNames the array of view names for the current virtualization
   * This will come from the virtualization.serviceViewDefinitions string[] array
   */
  viewNames: string[];

  /**
   * @param virtualizationName the name of the virtualization
   */
  virtualizationName: string;

  /**
   * @param onQueryResultsChanged the parameter function called by the parent component to that returns
   * the promotes the value of the latest query results
   */
  onQueryResultsChanged: (queryResults: QueryResults) => void;
}

export interface IViewSqlFormState {
  queryResults: QueryResults;
  querySql: string;
  viewName: string;
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
      querySql:
        'SELECT * FROM ' +
        (this.props.viewNames.length > 0
          ? this.props.viewNames[0]
          : '<view name>'),
      viewName: this.props.viewNames.length > 0 ? this.props.viewNames[0] : '',
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
    // TODO: change the defaultValue in the SqlStatement text area
    // to reflect the newly selected view name (if changed)
    // Maybe even show a text field showing the selected component depending on the PF4 widget type
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
            await queryVirtualization(
              this.props.virtualizationName,
              value.sqlStatement,
              value.rowLimit,
              value.rowOffset
            );
            // TODO: post toast notification
            alert(
              'Query successful. SQL : ' +
                JSON.stringify(value, undefined, 2) +
                '\nQuery Results: ' +
                JSON.stringify(this.state.queryResults, undefined, 2)
            );
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
                    this.props.viewNames.length > 0
                      ? 'Select View...'
                      : 'No Views Defined'
                  }
                  id="dropdown-example"
                  onClick={this.viewSelected}
                >
                  {this.props.viewNames.map((name, index) => (
                    <MenuItem key={index}>{name}</MenuItem>
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
