import { WithMonitoredIntegration } from '@syndesis/api';
import { PageLoader } from '@syndesis/ui';
import { WithLoader, WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { AppContext } from '../../../../app';
import { ApiError, PageTitle } from '../../../../shared';
import {
  IntegrationDetailHeader,
  WithIntegrationActions,
} from '../../components';
import resolvers from '../../resolvers';
import { ActivityPageTable } from './ActivityPageTable';
import { IDetailsRouteParams, IDetailsRouteState } from './interfaces';

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
                <WithRouteData<IDetailsRouteParams, IDetailsRouteState>>
                  {({ integrationId }, { integration }) => {
                    return (
                      <>
                        <WithMonitoredIntegration
                          integrationId={integrationId}
                          initialValue={integration}
                        >
                          {({ data, hasData, error, errorMessage }) => (
                            <WithLoader
                              error={error}
                              loading={!hasData}
                              loaderChildren={<PageLoader />}
                              errorChildren={<ApiError error={errorMessage!} />}
                            >
                              {() => (
                                <WithIntegrationActions
                                  integration={data.integration}
                                  postDeleteHref={resolvers.list()}
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
                                        <ActivityPageTable
                                          integrationId={data.integration.id!}
                                          linkToOpenShiftLog={
                                            getPodLogUrl(data.monitoring) || ''
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
