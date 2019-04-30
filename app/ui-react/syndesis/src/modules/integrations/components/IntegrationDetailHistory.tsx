import { IntegrationDeploymentOverview } from '@syndesis/models';
import {
  IntegrationActions,
  IntegrationDetailHistoryListViewItem,
} from '@syndesis/ui';
import * as React from 'react';
import { Translation } from 'react-i18next';

export interface IIntegrationDetailDeploymentsProps {
  actions: any;
  deployments: IntegrationDeploymentOverview[];
  integrationId: string;
  updatedAt: number;
  version: number;
}

export class IntegrationDetailHistory extends React.Component<
  IIntegrationDetailDeploymentsProps
> {
  public render() {
    const deployments = this.props.deployments ? this.props.deployments : [];
    return (
      <Translation ns={['integrations', 'shared']}>
        {t =>
          deployments.map((deployment, idx) => {
            return (
              <IntegrationDetailHistoryListViewItem
                key={idx}
                actions={
                  <IntegrationActions
                    integrationId={this.props.integrationId}
                    actions={this.props.actions}
                  />
                }
                currentState={deployment.currentState!}
                i18nTextLastPublished={t('integrations:detail:lastPublished')}
                i18nTextVersion={t('shared:Version')}
                updatedAt={deployment.updatedAt}
                version={deployment.version}
              />
            );
          })
        }
      </Translation>
    );
  }
}
