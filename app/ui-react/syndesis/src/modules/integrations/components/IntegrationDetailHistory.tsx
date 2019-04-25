import { IntegrationOverview } from '@syndesis/models';
import {
  IntegrationActions,
  IntegrationDetailHistoryListViewItem,
} from '@syndesis/ui';
import * as React from 'react';
import { Translation } from 'react-i18next';
import resolvers from '../resolvers';

export interface IIntegrationDetailDeploymentsProps {
  actions: any;
  integration: IntegrationOverview;
}

export class IntegrationDetailHistory extends React.Component<
  IIntegrationDetailDeploymentsProps
> {
  public render() {
    return (
      <Translation ns={['integrations', 'shared']}>
        {t =>
          this.props.integration.deployments!.map((deployment, idx) => {
            return (
              <IntegrationDetailHistoryListViewItem
                key={idx}
                actions={
                  <IntegrationActions
                    integrationId={this.props.integration.id!}
                    actions={this.props.actions}
                    detailsHref={resolvers.integration.details({
                      integration: this.props.integration,
                    })}
                  />
                }
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
