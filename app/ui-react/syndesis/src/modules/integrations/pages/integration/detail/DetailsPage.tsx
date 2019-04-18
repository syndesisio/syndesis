import { getSteps, WithIntegration } from '@syndesis/api';
import { Integration } from '@syndesis/models';
import {
  IntegrationDetailDescription,
  IntegrationDetailHistoryListView,
  IntegrationDetailInfo,
} from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
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
            <WithIntegration
              integrationId={integrationId}
              initialValue={integration}
            >
              <div>
                <Translation ns={['integration', 'shared']}>
                  {t => (
                    <>
                      <IntegrationDetailInfo
                        name={integration.name}
                        version={integration.version}
                      />
                      <IntegrationDetailNavBar integration={integration} />
                      <IntegrationDetailSteps
                        steps={getSteps(integration, 0)}
                      />
                      <IntegrationDetailDescription
                        description={integration.description}
                      />
                      <IntegrationDetailHistoryListView
                        integrationIsDraft={false}
                        children={integration.flows}
                        i18nTextDraft={t('Draft')}
                        i18nTextHistory={t('History')}
                      />
                    </>
                  )}
                </Translation>
              </div>
            </WithIntegration>
          );
        }}
      </WithRouteData>
    );
  }
}
