import {
  getSteps,
  WithIntegration,
  WithIntegrationHelpers,
} from '@syndesis/api';
import { Integration } from '@syndesis/models';
import {
  IntegrationDetailDescription,
  IntegrationDetailHistoryListView,
  IntegrationDetailHistoryListViewItem,
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
  integration?: Integration;
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
          console.log('integration: ' + JSON.stringify(integration));
          console.log('integrationId: ' + JSON.stringify(integrationId));

          return (
            <WithIntegrationHelpers>
              {({}) => {
                return (
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
                            <Translation ns={['integrations', 'shared']}>
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
                                    i18nNoDescription={t(
                                      'integrations:detail:noDescription'
                                    )}
                                  />
                                  <IntegrationDetailHistoryListView
                                    integrationIsDraft={false}
                                    i18nTextBtnEdit={t('shared:Edit')}
                                    i18nTextBtnPublish={t('shared:Publish')}
                                    i18nTextDraft={t('shared:Draft')}
                                    i18nTextHistory={t(
                                      'integrations:detail:History'
                                    )}
                                  >
                                    <IntegrationDetailHistoryListViewItem
                                      i18nTextHistoryMenuReplaceDraft={t(
                                        'integrations:detail:replaceDraft'
                                      )}
                                      i18nTextHistoryMenuUnpublish={t(
                                        'shared:Unpublish'
                                      )}
                                      i18nTextLastPublished={t(
                                        'integrations:detail:lastPublished'
                                      )}
                                      i18nTextVersion={t('shared:Version')}
                                      integrationUpdatedAt={data.updatedAt}
                                      integrationVersion={data.version}
                                    />
                                  </IntegrationDetailHistoryListView>
                                </>
                              )}
                            </Translation>
                          </div>
                        )}
                      </WithLoader>
                    )}
                  </WithIntegration>
                );
              }}
            </WithIntegrationHelpers>
          );
        }}
      </WithRouteData>
    );
  }
}
