import {
  canActivate,
  canDeactivate,
  WithIntegrationHelpers,
} from '@syndesis/api';
import {
  IntegrationMonitoring,
  IntegrationWithMonitoring,
  LeveledMessage,
} from '@syndesis/models';
import {
  IMenuActions,
  InlineTextEdit,
  IntegrationDetailBreadcrumb,
  IntegrationDetailEditableName,
  IntegrationDetailInfo,
  PageSection,
  SyndesisAlert,
  SyndesisAlertLevel,
} from '@syndesis/ui';
import * as React from 'react';
import { Translation } from 'react-i18next';
import resolvers from '../../resolvers';
import { IntegrationDetailNavBar } from './IntegrationDetailNavBar';

export interface IIntegrationDetailHeaderProps {
  data: IntegrationWithMonitoring;
  startAction: IMenuActions;
  stopAction: IMenuActions;
  deleteAction: IMenuActions;
  ciCdAction: IMenuActions;
  editAction: IMenuActions;
  exportAction: IMenuActions;
  getPodLogUrl: (
    monitoring: IntegrationMonitoring | undefined
  ) => string | undefined;
}

function toAlertLevel(level: 'ERROR' | 'WARN' | 'INFO') {
  switch (level) {
    case 'WARN':
      return SyndesisAlertLevel.WARN;
    case 'ERROR':
      return SyndesisAlertLevel.ERROR;
    default:
      return SyndesisAlertLevel.INFO;
  }
}

export const IntegrationDetailHeader: React.FunctionComponent<
  IIntegrationDetailHeaderProps
> = (props: IIntegrationDetailHeaderProps) => {
  const breadcrumbMenuActions: IMenuActions[] = [];
  if (canActivate(props.data.integration)) {
    breadcrumbMenuActions.push(props.startAction);
  }
  if (canDeactivate(props.data.integration)) {
    breadcrumbMenuActions.push(props.stopAction);
  }
  breadcrumbMenuActions.push(props.deleteAction);
  breadcrumbMenuActions.push(props.ciCdAction);

  return (
    <Translation ns={['integrations', 'shared']}>
      {t => {
        const bbMap = (
          (props.data.integration.board &&
            props.data.integration.board.messages) ||
          []
        ).reduce((acc, current, index, arr) => {
          try {
            if (!current) {
              return acc;
            }
            const key = current.code || current.message;
            if (!key || key === '') {
              return acc;
            }
            if (!(key in acc)) {
              acc[key] = current;
              return acc;
            }
            acc[key].detail += (
              acc[key].detail +
              '\n\n' +
              current.detail
            ).substring(0, 256);
          } catch (err) {
            // skip that one and keep going
          }
          return acc;
        }, {});
        const bulletinBoards = Object.keys(bbMap).map(
          (key: string) => bbMap[key]
        );
        return (
          <>
            <IntegrationDetailBreadcrumb
              editHref={props.editAction.href}
              editLabel={t('integrations:EditIntegration')}
              exportAction={props.exportAction.onClick}
              exportHref={props.exportAction.href}
              exportLabel={props.exportAction.label}
              homeHref={resolvers.dashboard.root()}
              i18nHome={t('shared:Home')}
              i18nIntegrations={t('shared:Integrations')}
              i18nPageTitle={t('integrations:detail:pageTitle')}
              integrationId={props.data.integration.id}
              integrationsHref={resolvers.integrations.list()}
              menuActions={breadcrumbMenuActions}
            />

            <PageSection variant={'light'}>
              <IntegrationDetailInfo
                name={
                  <WithIntegrationHelpers>
                    {({ setAttributes }) => {
                      const handleChange = async (name: string) => {
                        try {
                          await setAttributes(props.data.integration.id!, {
                            name,
                          });
                          return true;
                        } catch (err) {
                          return false;
                        }
                      };
                      return (
                        <IntegrationDetailEditableName
                          name={
                            <InlineTextEdit
                              value={props.data.integration.name}
                              allowEditing={true}
                              isTextArea={false}
                              onChange={handleChange}
                            />
                          }
                        />
                      );
                    }}
                  </WithIntegrationHelpers>
                }
                version={props.data.integration.version}
                currentState={props.data.integration.currentState!}
                targetState={props.data.integration.targetState!}
                monitoringValue={
                  props.data.monitoring &&
                  t('integrations:' + props.data.monitoring.detailedState.value)
                }
                monitoringCurrentStep={
                  props.data.monitoring &&
                  props.data.monitoring.detailedState.currentStep
                }
                monitoringTotalSteps={
                  props.data.monitoring &&
                  props.data.monitoring.detailedState.totalSteps
                }
                monitoringLogUrl={props.getPodLogUrl(props.data.monitoring)}
                i18nProgressPending={t('shared:Pending')}
                i18nProgressStarting={t('integrations:progressStarting')}
                i18nProgressStopping={t('integrations:progressStopping')}
                i18nLogUrlText={t('shared:viewLogs')}
              />
            </PageSection>
            {bulletinBoards.map((message: LeveledMessage, index) => (
              <PageSection variant={'light'} key={index}>
                <SyndesisAlert
                  level={toAlertLevel(message.level || 'INFO')}
                  message={
                    message.message
                      ? message.message
                      : t(`shared:${message.code!.toLocaleLowerCase()}`)
                  }
                  detail={message.detail}
                  i18nTextExpanded={t('shared:HideDetails')}
                  i18nTextCollapsed={t('shared:ShowDetails')}
                />
              </PageSection>
            ))}
            <PageSection variant={'light'} noPadding={true}>
              <IntegrationDetailNavBar integration={props.data.integration} />
            </PageSection>
          </>
        );
      }}
    </Translation>
  );
};
