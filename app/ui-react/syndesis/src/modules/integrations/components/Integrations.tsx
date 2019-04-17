import {
  canActivate,
  canDeactivate,
  canEdit,
  getFinishIcon,
  getStartIcon,
  WithIntegrationHelpers,
} from '@syndesis/api';
import { IntegrationWithMonitoring } from '@syndesis/models';
import {
  IIntegrationAction,
  IntegrationsList,
  IntegrationsListItem,
  IntegrationsListItemActions,
  IntegrationsListItemUnreadable,
  IntegrationsListSkeleton,
} from '@syndesis/ui';
import { WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { AppContext } from '../../../app';
import resolvers from '../resolvers';

export interface IIntegrationsProps {
  error: boolean;
  loading: boolean;
  integrations: IntegrationWithMonitoring[];
}

export class Integrations extends React.Component<IIntegrationsProps> {
  public render() {
    return (
      <Translation ns={['integrations', 'shared']}>
        {t => (
          <AppContext.Consumer>
            {({ config, getPodLogUrl }) => (
              <WithIntegrationHelpers>
                {({
                  deleteIntegration,
                  deployIntegration,
                  exportIntegration,
                  undeployIntegration,
                }) => (
                  <IntegrationsList>
                    <WithLoader
                      error={this.props.error}
                      loading={this.props.loading}
                      loaderChildren={<IntegrationsListSkeleton />}
                      errorChildren={<div>TODO</div>}
                    >
                      {() =>
                        this.props.integrations.map(
                          (mi: IntegrationWithMonitoring) => {
                            try {
                              const editAction: IIntegrationAction = {
                                href: resolvers.integration.edit.index({
                                  integration: mi.integration,
                                }),
                                label: 'Edit',
                              };
                              const startAction: IIntegrationAction = {
                                label: 'Start',
                                onClick: () =>
                                  deployIntegration(
                                    mi.integration.id!,
                                    mi.integration.version!,
                                    false
                                  ),
                              };
                              const stopAction: IIntegrationAction = {
                                label: 'Stop',
                                onClick: () =>
                                  undeployIntegration(
                                    mi.integration.id!,
                                    mi.integration.version!
                                  ),
                              };
                              const deleteAction: IIntegrationAction = {
                                label: 'Delete',
                                onClick: () =>
                                  deleteIntegration(mi.integration.id!),
                              };
                              const exportAction: IIntegrationAction = {
                                label: 'Export',
                                onClick: () =>
                                  exportIntegration(
                                    mi.integration.id!,
                                    `${mi.integration.name}-export.zip`
                                  ),
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
                                  key={mi.integration.id}
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
                                  startConnectionIcon={getStartIcon(
                                    config.apiEndpoint,
                                    mi.integration
                                  )}
                                  finishConnectionIcon={getFinishIcon(
                                    config.apiEndpoint,
                                    mi.integration
                                  )}
                                  actions={
                                    <IntegrationsListItemActions
                                      integrationId={mi.integration!.id!}
                                      actions={actions}
                                      detailsHref={resolvers.integration.details(
                                        { integration: mi.integration }
                                      )}
                                    />
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
                            } catch (e) {
                              return (
                                <IntegrationsListItemUnreadable
                                  key={mi.integration.id}
                                  integrationName={
                                    (mi &&
                                      mi.integration &&
                                      mi.integration.name) ||
                                    'An integration'
                                  }
                                  i18nDescription={
                                    "Sorry, we can't display more information about this integration right now."
                                  }
                                  rawObject={JSON.stringify(
                                    mi.integration,
                                    null,
                                    2
                                  )}
                                />
                              );
                            }
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
      </Translation>
    );
  }
}
