import { WithIntegrationHelpers } from '@syndesis/api';
import { IntegrationWithMonitoring } from '@syndesis/models';
import {
  IntegrationsList,
  IntegrationsListItem,
  IntegrationsListSkeleton,
} from '@syndesis/ui';
import { WithLoader } from '@syndesis/utils';
import * as H from 'history';
import { DropdownKebab } from 'patternfly-react';
import * as React from 'react';
import { NamespacesConsumer } from 'react-i18next';
import { Link } from 'react-router-dom';
import { AppContext } from '../../../app';

export interface IIntegrationAction {
  href?: H.LocationDescriptor;
  onClick?: (e: React.MouseEvent<any>) => any;
  label: string | JSX.Element;
}

export interface IIntegrationsProps {
  error: boolean;
  loading: boolean;
  integrations: IntegrationWithMonitoring[];
}

export class Integrations extends React.Component<IIntegrationsProps> {
  public render() {
    return (
      <NamespacesConsumer ns={['integrations', 'shared']}>
        {t => (
          <AppContext.Consumer>
            {({ config, getPodLogUrl }) => (
              <WithIntegrationHelpers>
                {({ canActivate, canDeactivate, canEdit }) => (
                  <IntegrationsList>
                    <WithLoader
                      error={this.props.error}
                      loading={this.props.loading}
                      loaderChildren={
                        <>
                          <div className={'list-group-item'}>
                            <div>
                              <IntegrationsListSkeleton />
                            </div>
                          </div>
                          <div className={'list-group-item'}>
                            <div>
                              <IntegrationsListSkeleton />
                            </div>
                          </div>
                          <div className={'list-group-item'}>
                            <div>
                              <IntegrationsListSkeleton />
                            </div>
                          </div>
                        </>
                      }
                      errorChildren={<div>TODO</div>}
                    >
                      {() =>
                        this.props.integrations.map(
                          (mi: IntegrationWithMonitoring, index) => {
                            const editAction: IIntegrationAction = {
                              href: '#todo',
                              label: 'Edit',
                            };
                            const startAction: IIntegrationAction = {
                              label: 'Start',
                              onClick: () => alert('todo: start'),
                            };
                            const stopAction: IIntegrationAction = {
                              label: 'Stop',
                              onClick: () => alert('todo: stop'),
                            };
                            const deleteAction: IIntegrationAction = {
                              label: 'Delete',
                              onClick: () => alert('todo: delete'),
                            };
                            const exportAction: IIntegrationAction = {
                              label: 'Export',
                              onClick: () => alert('todo: export'),
                            };
                            const actions: IIntegrationAction[] = [];
                            if (canEdit(mi.integration)) {
                              actions.push(editAction);
                            }
                            if (canActivate(mi.integration)) {
                              actions.push(startAction);
                            }
                            if (canDeactivate(mi.integration)) {
                              actions.push(stopAction);
                            }
                            actions.push(deleteAction);
                            actions.push(exportAction);
                            return (
                              <IntegrationsListItem
                                key={index}
                                integrationId={mi.integration.id!}
                                integrationName={mi.integration.name}
                                currentState={mi.integration!.currentState!}
                                targetState={mi.integration!.targetState!}
                                isConfigurationRequired={
                                  !!(
                                    mi.integration!.board!.warnings ||
                                    mi.integration!.board!.errors ||
                                    mi.integration!.board!.notices
                                  )
                                }
                                monitoringValue={
                                  mi.monitoring &&
                                  t(
                                    'integrations:' +
                                      mi.monitoring.detailedState.value
                                  )
                                }
                                monitoringCurrentStep={
                                  mi.monitoring &&
                                  mi.monitoring.detailedState.currentStep
                                }
                                monitoringTotalSteps={
                                  mi.monitoring &&
                                  mi.monitoring.detailedState.totalSteps
                                }
                                monitoringLogUrl={getPodLogUrl(
                                  config,
                                  mi.monitoring
                                )}
                                startConnectionIcon={
                                  mi.integration.flows![0].steps![0].connection!
                                    .icon!
                                }
                                finishConnectionIcon={
                                  mi.integration.flows![0].steps![
                                    mi.integration.flows![0].steps!.length - 1
                                  ].connection!.icon!
                                }
                                actions={
                                  <>
                                    <Link
                                      to={'#todo'}
                                      className={'btn btn-default'}
                                    >
                                      View
                                    </Link>
                                    <DropdownKebab
                                      id={`integration-${
                                        mi.integration!.id
                                      }-action-menu`}
                                      pullRight={true}
                                    >
                                      {actions.map((a, idx) => (
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
                                      ))}
                                    </DropdownKebab>
                                  </>
                                }
                                i18nConfigurationRequired={t(
                                  'integrations:ConfigurationRequired'
                                )}
                                i18nError={t('shared:Error')}
                                i18nPublished={t('shared:Published')}
                                i18nUnpublished={t('shared:Unpublished')}
                                i18nProgressPending={t('shared:Pending')}
                                i18nProgressStarting={t(
                                  'integrations:progressStarting'
                                )}
                                i18nProgressStopping={t(
                                  'integrations:progressStopping'
                                )}
                                i18nLogUrlText={t('shared:viewLogs')}
                              />
                            );
                          }
                        )
                      }
                    </WithLoader>
                  </IntegrationsList>
                )}
              </WithIntegrationHelpers>
            )}
          </AppContext.Consumer>
        )}
      </NamespacesConsumer>
    );
  }
}
