import { useVirtualizationHelpers } from '@syndesis/api';
import { AutoForm, IFormDefinition, IFormValue } from '@syndesis/auto-form';
import * as H from '@syndesis/history';
import {
  QueryResults,
  ViewDefinition,
  ViewDefinitionDescriptor,
} from '@syndesis/models';
import { SqlClientContent, SqlClientForm } from '@syndesis/ui';
import { useContext } from 'react';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { UIContext } from '../../../app';
import {
  getPreviewSql,
  getQueryColumns,
  getQueryRows,
} from './VirtualizationUtils';

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
  views: ViewDefinitionDescriptor[];

  virtualizationId: string;

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

/**
 * A component to generate the SqlClient page content
 */
export const WithVirtualizationSqlClientForm: React.FunctionComponent<
  IWithVirtualizationSqlClientFormProps
> = props => {
  const { t } = useTranslation(['data', 'shared']);
  const { pushNotification } = useContext(UIContext);
  const { getViewDefinition, queryVirtualization } = useVirtualizationHelpers();

  const queryResultsEmpty: QueryResults = {
    columns: [],
    rows: [],
  };
  const [queryResults, setQueryResults] = React.useState(queryResultsEmpty);

  const buildViews = () => {
    const enums = [];
    for (const view of props.views) {
      enums.push({ label: view.name, value: view.name });
    }
    return enums;
  };

  const getInitialView = () => {
    return props.views.length > 0 ? props.views[0].name : '';
  };

  const formDefinition = {
    rowLimit: {
      componentProperty: true,
      deprecated: false,
      description: t('data:virtualization.viewSqlFormRowLimitDescription'),
      displayName: t('data:virtualization.viewSqlFormRowLimit'),
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
      description: t('data:virtualization.viewSqlFormRowOffsetDescription'),
      displayName: t('data:virtualization.viewSqlFormRowOffset'),
      javaType: 'java.lang.Integer',
      kind: 'property',
      order: 3,
      required: false,
      secret: false,
      type: 'number',
    },
    view: {
      description: t('virtualization.viewSqlFormViewDescription'),
      displayName: t('virtualization.View'),
      enum: buildViews(),
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
    view: getInitialView(),
  };

  // The purpose of this function is to reset the query results
  //  whenever a form selection is changed
  const validate = (values: IFormValue) => {
    setQueryResults(queryResultsEmpty);
    return {};
  };

  const doSubmit = async (value: any) => {
    const selectedViewName = value.view ? value.view : getInitialView();
    const viewDefn = props.views.find(view => view.name === selectedViewName);
    try {
      let sqlStatement = '';
      if (viewDefn) {
        const viewDefinition: ViewDefinition = await getViewDefinition(
          viewDefn.id
        );
        sqlStatement = getPreviewSql(viewDefinition);
      }
      const results: QueryResults = await queryVirtualization(
        props.virtualizationId,
        sqlStatement,
        value.rowLimit,
        value.rowOffset
      );
      setQueryResults(results);
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
      {({ fields, handleSubmit, isSubmitting, isValid, submitForm }) => (
        <SqlClientContent
          formContent={
            <SqlClientForm handleSubmit={handleSubmit} i18nSubmit={t('Submit')}>
              {fields}
            </SqlClientForm>
          }
          viewNames={props.views.map(
            (viewDefn: ViewDefinitionDescriptor) => viewDefn.name
          )}
          queryResultRows={getQueryRows(queryResults)}
          queryResultCols={getQueryColumns(queryResults)}
          i18nResultsTitle={t('data:virtualization.queryResultsTitle')}
          i18nResultsRowCountMsg={t(
            'data:virtualization.queryResultsRowCountMsg'
          )}
          i18nEmptyStateInfo={t('data:virtualization.viewEmptyStateInfo')}
          i18nEmptyStateTitle={t('data:virtualization.viewEmptyStateTitle')}
          i18nImportViews={t('data:virtualization.importDataSource')}
          i18nImportViewsTip={t('data:virtualization.importDataSourceTip')}
          i18nCreateView={t('data:virtualization.createView')}
          i18nCreateViewTip={t('data:virtualization.createViewTip')}
          linkCreateViewHRef={props.linkCreateView}
          linkImportViewsHRef={props.linkImportViews}
          i18nEmptyResultsTitle={t(
            'data:virtualization.queryResultsTableEmptyStateTitle'
          )}
          i18nEmptyResultsMsg={t(
            'data:virtualization.queryResultsTableEmptyStateInfo'
          )}
        />
      )}
    </AutoForm>
  );
};
