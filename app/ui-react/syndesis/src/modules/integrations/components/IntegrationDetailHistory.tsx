import { IntegrationDeploymentOverview } from '@syndesis/models';
import {
  IIntegrationAction,
  IntegrationDetailHistoryListViewItem,
} from '@syndesis/ui';
import { DropdownKebab } from 'patternfly-react';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { Link } from 'react-router-dom';

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
                  <DropdownKebab
                    id={`integration-${this.props.integrationId}-action-menu`}
                    pullRight={true}
                  >
                    {this.props.actions.map(
                      (a: IIntegrationAction, idx: number) => (
                        <li role={'presentation'} key={idx}>
                          {a.href ? (
                            <Link
                              to={a.href}
                              onClick={a.onClick}
                              role={'menuitem'}
                              tabIndex={idx + 1}
                            >
                              {a.label}
                            </Link>
                          ) : (
                            <a
                              href={'javascript:void(0)'}
                              onClick={a.onClick}
                              role={'menuitem'}
                              tabIndex={idx + 1}
                            >
                              {a.label}
                            </a>
                          )}
                        </li>
                      )
                    )}
                  </DropdownKebab>
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
