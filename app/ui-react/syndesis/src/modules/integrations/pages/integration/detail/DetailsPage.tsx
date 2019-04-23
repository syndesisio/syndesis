import {
  getSteps,
  WithIntegration,
  WithIntegrationHelpers,
} from '@syndesis/api';
import { Integration } from '@syndesis/models';
import {
  IntegrationDetailDescription,
  IntegrationDetailHistoryListView,
  // IntegrationDetailHistoryListViewItem,
  IntegrationDetailInfo,
  Loader,
} from '@syndesis/ui';
import { WithLoader, WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { IntegrationDetailSteps } from '../../../components';
import { IntegrationDetailNavBar } from '../../../shared';

/**
 * @integrationId - the ID of the integration for which details are being displayed.
 */
export interface IDetailsPageParams {
  integration: Integration;
  integrationId: string;
}

export interface IDetailsPageState {
  integration: Integration;
}

/**
 * This page shows the first, and default, tab of the Integration Detail page.
 *
 * This component expects either an integrationId in the URL,
 * or an integration object set via the state.
 *
 */
export class DetailsPage extends React.Component {
  public render() {
    return (
      <WithRouteData<IDetailsPageParams, IDetailsPageState>>
        {({ integrationId }, { integration }, { history }) => {
          return (
            <WithIntegrationHelpers>
              {({
                deleteIntegration,
                deployIntegration,
                exportIntegration,
                undeployIntegration,
              }) => (
                <>
                  <WithIntegration
                    integrationId={integrationId}
                    initialValue={integration}
                  >
                    {({ data, hasData, error }) => (
                      <WithLoader
                        error={error}
                        loading={!hasData}
                        loaderChildren={<Loader />}
                        errorChildren={<div>TODO</div>}
                      >
                        {() => (
                          <div>
                            <Translation ns={['integration', 'shared']}>
                              {t => (
                                <>
                                  <IntegrationDetailInfo
                                    name={data.name}
                                    version={data.version}
                                  />
                                  <IntegrationDetailNavBar integration={data} />
                                  <IntegrationDetailSteps
                                    steps={getSteps(data, 0)}
                                  />
                                  <IntegrationDetailDescription
                                    description={data.description}
                                  />
                                  <IntegrationDetailHistoryListView
                                    integrationIsDraft={false}
                                    i18nTextDraft={t('Draft')}
                                    i18nTextHistory={t('History')}
                                  />
                                </>
                              )}
                            </Translation>
                          </div>
                        )}
                      </WithLoader>
                    )}
                  </WithIntegration>
                </>
              )}
            </WithIntegrationHelpers>
          );
        }}
      </WithRouteData>
    );
  }
}
