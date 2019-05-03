import { canActivate, canDeactivate } from '@syndesis/api';
import {
  IntegrationMonitoring,
  IntegrationWithMonitoring,
} from '@syndesis/models';
import {
  IMenuActions,
  IntegrationDetailBreadcrumb,
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
              name={props.data.integration.name}
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
          <PageSection variant={'light'} noPadding={true}>
            <IntegrationDetailNavBar integration={props.data.integration} />
          </PageSection>
        </>
      )}
    </Translation>
  );
};
