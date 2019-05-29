import { WithViewEditorStates } from '@syndesis/api';
import { RestDataService, ViewEditorState } from '@syndesis/models';
import { Breadcrumb, PageSection, ViewHeader } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { Link } from 'react-router-dom';
import resolvers from '../../resolvers';
import {
  VirtualizationNavBar,
  WithVirtualizationSqlClientForm,
} from '../shared/';
import { getPreviewVdbName } from '../shared/VirtualizationUtils';

/**
 * @param virtualizationId - the ID of the virtualization shown by this page.
 */
export interface IVirtualizationSqlClientPageRouteParams {
  virtualizationId: string;
  virtualization: RestDataService;
}

/**
 * @param virtualization - the virtualization being shown by this page. If
 * exists, it must equal to the [virtualizationId]{@link IVirtualizationSqlClientPageRouteParams#virtualizationId}.
 */
export interface IVirtualizationSqlClientPageRouteState {
  virtualization: RestDataService;
}

/**
 * Page displays virtualization views and allows user run test queries against the views.
 */
export class VirtualizationSqlClientPage extends React.Component<
  IVirtualizationSqlClientPageRouteState
> {
  public handleSubmit() {
    // TODO: finish form handling
  }

  public render() {
    return (
      <WithRouteData<
        IVirtualizationSqlClientPageRouteParams,
        IVirtualizationSqlClientPageRouteState
      >>
        {({ virtualizationId }, { virtualization }, { history }) => (
          <Translation ns={['data', 'shared']}>
            {t => (
              <>
                <Breadcrumb>
                  <Link
                    data-testid={'virtualization-sql-client-page-home-link'}
                    to={resolvers.dashboard.root()}
                  >
                    {t('shared:Home')}
                  </Link>
                  <Link
                    data-testid={
                      'virtualization-sql-client-page-virtualizations-link'
                    }
                    to={resolvers.data.root()}
                  >
                    {t('shared:DataVirtualizations')}
                  </Link>
                  <span>
                    {virtualizationId + ' '}
                    {t('data:virtualization.sqlClient')}
                  </span>
                </Breadcrumb>
                <ViewHeader
                  i18nTitle={virtualization.keng__id}
                  i18nDescription={virtualization.tko__description}
                />
                <PageSection variant={'light'} noPadding={true}>
                  <VirtualizationNavBar virtualization={virtualization} />
                </PageSection>
                <WithViewEditorStates
                  idPattern={virtualization.serviceVdbName + '*'}
                >
                  {({ data, hasData, error }) => (
                    <WithVirtualizationSqlClientForm
                      views={data.map(
                        (editorState: ViewEditorState) =>
                          editorState.viewDefinition
                      )}
                      targetVdb={getPreviewVdbName()}
                      linkCreateView={resolvers.data.virtualizations.create()}
                      linkImportViews={resolvers.data.virtualizations.views.importSource.selectConnection(
                        { virtualization }
                      )}
                    >
                      {({ form, submitForm, isSubmitting }) => <></>}
                    </WithVirtualizationSqlClientForm>
                  )}
                </WithViewEditorStates>
              </>
            )}
          </Translation>
        )}
      </WithRouteData>
    );
  }
}
