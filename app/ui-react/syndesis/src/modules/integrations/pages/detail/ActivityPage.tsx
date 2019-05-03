import { WithMonitoredIntegration } from '@syndesis/api';
import { Integration } from '@syndesis/models';
import { IntegrationDetailActivity, Loader } from '@syndesis/ui';
import { WithLoader, WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { AppContext } from '../../../../app';
import { ApiError, PageTitle } from '../../../../shared';
import {
  IntegrationDetailHeader,
  WithIntegrationActions,
} from '../../components';
import { ActivityPageTable } from './ActivityPageTable';

/**
 * @integrationId - the ID of the integration for which details are being displayed.
 */
export interface IActivityPageParams {
  integrationId: string;
}

export interface IActivityPageState {
  integration: Integration;
}

/**
 * This page shows the second tab of the Integration Detail page.
 *
 * This component expects either an integrationId in the URL,
 * or an integration object set via the state.
 *
 */
export class ActivityPage extends React.Component {
  public render() {
    return (
      <>
        <Translation ns={['integrations', 'shared']}>
          {t => (
            <AppContext.Consumer>
              {({ getPodLogUrl }) => (
                <WithRouteData<IActivityPageParams, IActivityPageState>>
                  {({ integrationId }, { integration }, { history }) => {
                    return (
                      <>
                        <WithMonitoredIntegration integrationId={integrationId}>
                          {({ data, hasData, error }) => (
                            <WithLoader
                              error={error}
                              loading={!hasData}
                              loaderChildren={<Loader />}
                              errorChildren={<ApiError />}
                            >
                              {() => (
                                <WithIntegrationActions
                                  integration={data.integration}
                                >
                                  {({
                                    ciCdAction,
                                    editAction,
                                    deleteAction,
                                    exportAction,
                                    startAction,
                                    stopAction,
                                  }) => {
                                    return (
                                      <>
                                        <PageTitle
                                          title={t(
                                            'integrations:detail:pageTitle'
                                          )}
                                        />
                                        <IntegrationDetailHeader
                                          data={data}
                                          startAction={startAction}
                                          stopAction={stopAction}
                                          deleteAction={deleteAction}
                                          ciCdAction={ciCdAction}
                                          editAction={editAction}
                                          exportAction={exportAction}
                                          getPodLogUrl={getPodLogUrl}
                                        />
                                        <IntegrationDetailActivity
                                          i18nBtnRefresh={t('shared:Refresh')}
                                          i18nLastRefresh={t(
                                            'integrations:LastRefresh'
                                          )}
                                          i18nViewLogOpenShift={t(
                                            'integrations:View Log in OpenShift'
                                          )}
                                          linkToOpenShiftLog={
                                            getPodLogUrl(data.monitoring) || ''
                                          }
                                          children={
                                            <ActivityPageTable
                                              integrationId={
                                                data.integration.id!
                                              }
                                            />
                                          }
                                        />
                                      </>
                                    );
                                  }}
                                </WithIntegrationActions>
                              )}
                            </WithLoader>
                          )}
                        </WithMonitoredIntegration>
                      </>
                    );
                  }}
                </WithRouteData>
              )}
            </AppContext.Consumer>
          )}
        </Translation>
      </>
    );
  }
}
