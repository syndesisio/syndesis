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
  AlertLevel,
  IMenuActions,
  InlineTextEdit,
  IntegrationBulletinBoardAlert,
  IntegrationDetailBreadcrumb,
  IntegrationDetailEditableName,
  IntegrationDetailInfo,
  PageSection,
} from '@syndesis/ui';
import * as React from 'react';
import { Translation } from 'react-i18next';
import resolvers from '../../resolvers';
import { IntegrationDetailNavBar } from '../shared';

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
      return AlertLevel.WARN;
    case 'ERROR':
      return AlertLevel.ERROR;
    default:
      return AlertLevel.INFO;
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
      {t => (
        <>
          <IntegrationDetailBreadcrumb
            editHref={props.editAction.href}
            editLabel={props.editAction.label}
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
          <PageSection variant={'light'}>
            {(
              (props.data.integration.board &&
                props.data.integration.board.messages) ||
              []
            ).map((message: LeveledMessage, index) => (
              <IntegrationBulletinBoardAlert
                key={index}
                level={toAlertLevel(message.level || 'INFO')}
                message={t(`shared:${message.code!.toLocaleLowerCase()}`)}
                detail={message.detail}
                i18nTextExpanded={t('shared:HideDetails')}
                i18nTextCollapsed={t('shared:ShowDetails')}
              />
            ))}
          </PageSection>
          <PageSection variant={'light'} noPadding={true}>
            <IntegrationDetailNavBar integration={props.data.integration} />
          </PageSection>
        </>
      )}
    </Translation>
  );
};
