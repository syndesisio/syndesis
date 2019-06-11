import { WithVirtualizationHelpers } from '@syndesis/api';
import { AutoForm, IFormDefinition, IFormValue } from '@syndesis/auto-form';
import * as H from '@syndesis/history';
import { QueryResults, ViewDefinition } from '@syndesis/models';
import { SqlClientContent, SqlClientForm } from '@syndesis/ui';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { UIContext } from '../../../app';
import i18n from '../../../i18n';
import { getPreviewSql } from './VirtualizationUtils';

export interface IWithVirtualizationSqlClientFormChildrenProps {
  /**
   * the form (embedded in the right UI elements)
   */
  form: JSX.Element;
  /**
   * true if the form contains valid values. Can be used to enable/disable the
   * submit button.
   */
  isValid: boolean;
  /**
   * true if the form is being submitted. Can be used to enable/disable the
   * submit button.
   */
  isSubmitting: boolean;
  /**
   * the callback to fire to submit the form.
   */
  submitForm(): any;
}

export interface IWithVirtualizationSqlClientFormProps {
  views: ViewDefinition[];

  targetVdb: string;

  linkCreateView: H.LocationDescriptor;
  linkImportViews: H.LocationDescriptor;

  /**
   * the render prop that will receive the ready-to-be-rendered form and some
   * helpers.
   *
   * @see [form]{@link IWithVirtualizationSqlClientFormChildrenProps#form}
   * @see [isValid]{@link IWithVirtualizationSqlClientFormChildrenProps#isValid}
   * @see [isSubmitting]{@link IWithVirtualizationSqlClientFormChildrenProps#isSubmitting}
   * @see [onSubmit]{@link IWithVirtualizationSqlClientFormChildrenProps#submitForm}
   */
  // tslint:disable-next-line: react-unused-props-and-state
  children(props: IWithVirtualizationSqlClientFormChildrenProps): any;
}

export interface IWithVirtualizationSqlClientFormState {
  queryResults: QueryResults;
}

interface IColumn {
  id: string;
  label: string;
}

/**
 * A component to generate the SqlClient page content
 */
export class WithVirtualizationSqlClientForm extends React.Component<
  IWithVirtualizationSqlClientFormProps,
  IWithVirtualizationSqlClientFormState
> {
  public static queryResultsEmpty: QueryResults = {
    columns: [],
    rows: [],
  };

  constructor(props: IWithVirtualizationSqlClientFormProps) {
    super(props);
    this.state = {
      queryResults: WithVirtualizationSqlClientForm.queryResultsEmpty,
    };

    this.setQueryResults = this.setQueryResults.bind(this);
  }

  public setQueryResults(results: QueryResults) {
    results && results.columns && results.columns.length > 0
      ? this.setState({
          queryResults: results,
        })
      : this.setState({
          queryResults: WithVirtualizationSqlClientForm.queryResultsEmpty,
        });
  }

  public buildViews() {
    const enums = [];
    for (const view of this.props.views) {
      enums.push({ label: view.viewName, value: view.viewName });
    }
    return enums;
  }

  public buildRows(queryResults: QueryResults): Array<{}> {
    const allRows = queryResults.rows ? queryResults.rows : [];
    return allRows
      .map(row => row.row)
      .map(row =>
        row.reduce(
          // tslint:disable-next-line: no-shadowed-variable
          (row, r, idx) => ({
            ...row,
            [this.state.queryResults.columns[idx].name]: r,
          }),
          {}
        )
      );
  }

  public buildColumns(queryResults: QueryResults): IColumn[] {
    const columns = [];
    if (queryResults.columns) {
      for (const col of queryResults.columns) {
        columns.push({ id: col.name, label: col.label });
      }
    }
    return columns;
  }

  public getInitialView() {
    return this.props.views.length > 0 ? this.props.views[0].viewName : '';
  }

  public render() {
    const formDefinition = {
      rowLimit: {
        componentProperty: true,
        deprecated: false,
        description: i18n.t(
          'data:virtualization.viewSqlFormRowLimitDescription'
        ),
        displayName: i18n.t('data:virtualization.viewSqlFormRowLimit'),
        javaType: 'java.lang.Integer',
        kind: 'property',
        order: 2,
        required: true,
        secret: false,
        type: 'number',
      },
      rowOffset: {
        componentProperty: true,
        deprecated: false,
        description: i18n.t(
          'data:virtualization.viewSqlFormRowOffsetDescription'
        ),
        displayName: i18n.t('data:virtualization.viewSqlFormRowOffset'),
        javaType: 'java.lang.Integer',
        kind: 'property',
        order: 3,
        required: false,
        secret: false,
        type: 'number',
      },
      view: {
        description: 'The View to Query',
        displayName: 'View',
        enum: this.buildViews(),
        kind: 'parameter',
        order: 1,
        required: true,
        secret: false,
        type: 'string',
      },
      // TODO - Future add this for SQL ad-hoc queries...
      // sqlStatement: {
      //   componentProperty: true,
      //   defaultValue: 'default query sql',
      //   deprecated: false,
      //   description: i18n.t(
      //     'data:virtualization.viewSqlFormSqlStatementDescription'
      //   ),
      //   displayName: i18n.t('data:virtualization.viewSqlFormSqlStatement'),
      //   height: 300,
      //   javaType: 'java.lang.String',
      //   kind: 'property',
      //   order: 2,
      //   required: true,
      //   secret: false,
      //   type: 'textarea',
      // },
    } as IFormDefinition;

    const initialValue = {
      rowLimit: '15',
      rowOffset: '0',
      view: this.getInitialView(),
    };

    // The purpose of this function is to reset the query results
    //  whenever a form selection is changed
    const validate = (values: IFormValue) => {
      this.setQueryResults(WithVirtualizationSqlClientForm.queryResultsEmpty);
      return {};
    };

    return (
      <Translation ns={['data', 'shared']}>
        {t => (
          <UIContext.Consumer>
            {({ pushNotification }) => {
              return (
                <WithVirtualizationHelpers>
                  {({ queryVirtualization }) => {
                    const doSubmit = async (value: any) => {
                      const selectedViewName = value.view
                        ? value.view
                        : this.getInitialView();
                      const viewDefn = this.props.views.find(
                        view => view.viewName === selectedViewName
                      );
                      try {
                        let sqlStatement = '';
                        if (viewDefn) {
                          sqlStatement = getPreviewSql(viewDefn);
                        }
                        const results: QueryResults = await queryVirtualization(
                          this.props.targetVdb,
                          sqlStatement,
                          value.rowLimit,
                          value.rowOffset
                        );
                        pushNotification(
                          t('virtualization.queryViewSuccess', {
                            name: value.viewName,
                          }),
                          'success'
                        );
                        this.setQueryResults(results);
                      } catch (error) {
                        const details = error.message ? error.message : '';
                        pushNotification(
                          t('virtualization.queryViewFailed', {
                            details,
                            name: value.viewName,
                          }),
                          'error'
                        );
                      }
                    };
                    return (
                      <AutoForm
                        i18nRequiredProperty={t('shared:requiredFieldMessage')}
                        definition={formDefinition}
                        initialValue={initialValue}
                        validate={validate}
                        onSave={(properties, actions) => {
                          doSubmit(properties).finally(() => {
                            actions.setSubmitting(false);
                          });
                        }}
                      >
                        {({
                          fields,
                          handleSubmit,
                          isSubmitting,
                          isValid,
                          submitForm,
                        }) => (
                          <SqlClientContent
                            formContent={
                              <SqlClientForm handleSubmit={handleSubmit}>
                                {fields}
                              </SqlClientForm>
                            }
                            viewNames={this.props.views.map(
                              (viewDefn: ViewDefinition) => viewDefn.viewName
                            )}
                            queryResultRows={this.buildRows(
                              this.state.queryResults
                            )}
                            queryResultCols={this.buildColumns(
                              this.state.queryResults
                            )}
                            targetVdb={'test'}
                            i18nResultsTitle={i18n.t(
                              'data:virtualization.queryResultsTitle'
                            )}
                            i18nResultsRowCountMsg={i18n.t(
                              'data:virtualization.queryResultsRowCountMsg'
                            )}
                            i18nEmptyStateInfo={i18n.t(
                              'data:virtualization.viewEmptyStateInfo'
                            )}
                            i18nEmptyStateTitle={i18n.t(
                              'data:virtualization.viewEmptyStateTitle'
                            )}
                            i18nImportViews={i18n.t(
                              'data:virtualization.importDataSource'
                            )}
                            i18nImportViewsTip={i18n.t(
                              'data:virtualization.importDataSourceTip'
                            )}
                            i18nCreateView={i18n.t(
                              'data:virtualization.createView'
                            )}
                            i18nCreateViewTip={i18n.t(
                              'data:virtualization.createViewTip'
                            )}
                            linkCreateViewHRef={this.props.linkCreateView}
                            linkImportViewsHRef={this.props.linkImportViews}
                            i18nEmptyResultsTitle={i18n.t(
                              'data:virtualization.queryResultsTableEmptyStateTitle'
                            )}
                            i18nEmptyResultsMsg={i18n.t(
                              'data:virtualization.queryResultsTableEmptyStateInfo'
                            )}
                          />
                        )}
                      </AutoForm>
                    );
                  }}
                </WithVirtualizationHelpers>
              );
            }}
          </UIContext.Consumer>
        )}
      </Translation>
    );
  }
}
