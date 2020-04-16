import {
  DataListAction,
  DataListCell,
  DataListItem,
  DataListItemCells,
  DataListItemRow,
} from '@patternfly/react-core';
import { WarningTriangleIcon } from '@patternfly/react-icons';
import { global_warning_color_100 } from '@patternfly/react-tokens';
import * as React from 'react';
import { toValidHtmlId } from '../helpers';
import { IntegrationIcon } from './IntegrationIcon';
import { IntegrationStatus } from './IntegrationStatus';
import { IntegrationStatusDetail } from './IntegrationStatusDetail';
import { IntegrationState } from './models';

import './IntegrationsListItem.css';

export interface IIntegrationsListItemProps {
  integrationName: string;
  currentState: IntegrationState;
  targetState: IntegrationState;
  isConfigurationRequired: boolean;
  monitoringValue?: string;
  monitoringCurrentStep?: number;
  monitoringTotalSteps?: number;
  monitoringLogUrl?: string;
  startConnectionIcon: React.ReactNode;
  finishConnectionIcon: React.ReactNode;
  actions: any;
  i18nConfigurationRequired: string;
  i18nError: string;
  i18nPublished: string;
  i18nProgressPending: string;
  i18nProgressStarting: string;
  i18nProgressStopping: string;
  i18nUnpublished: string;
  i18nLogUrlText: string;
  checkboxComponent?: React.ReactNode;
}

export const IntegrationsListItem: React.FunctionComponent<IIntegrationsListItemProps> = ({
  actions,
  checkboxComponent,
  currentState,
  finishConnectionIcon,
  i18nConfigurationRequired,
  i18nError,
  i18nLogUrlText,
  i18nProgressPending,
  i18nProgressStarting,
  i18nProgressStopping,
  i18nPublished,
  i18nUnpublished,
  integrationName,
  isConfigurationRequired,
  monitoringCurrentStep,
  monitoringLogUrl,
  monitoringTotalSteps,
  monitoringValue,
  startConnectionIcon,
  targetState,
}) => {
  const id = toValidHtmlId(integrationName);
  return (
    <DataListItem
      data-testid={`integrations-list-item-${id}-list-item`}
      className={'integration-list-item'}
      aria-labelledby={`integration-list-item-${id}-title`}
    >
      <DataListItemRow>
        {checkboxComponent ? checkboxComponent : null}
        <DataListItemCells
          dataListCells={[
            <DataListCell width={1} key={0}>
              <div className={'integration-list-item__icon-wrapper'}>
                <IntegrationIcon
                  startConnectionIcon={startConnectionIcon}
                  finishConnectionIcon={finishConnectionIcon}
                />
              </div>
            </DataListCell>,
            <DataListCell width={3} key={1}>
              <div
                id={`integration-list-item-${id}-title`}
                className={'integration-list-item__text-wrapper'}
                data-testid={'integration-name'}
              >
                {integrationName}
              </div>
            </DataListCell>,
            <DataListCell
              key={2}
              className={'integration-list-item__additional-info'}
              width={1}
            >
              {currentState === 'Pending' ? (
                <IntegrationStatusDetail
                  targetState={targetState}
                  value={monitoringValue}
                  currentStep={monitoringCurrentStep}
                  totalSteps={monitoringTotalSteps}
                  logUrl={monitoringLogUrl}
                  i18nProgressPending={i18nProgressPending}
                  i18nProgressStarting={i18nProgressStarting}
                  i18nProgressStopping={i18nProgressStopping}
                  i18nLogUrlText={i18nLogUrlText}
                />
              ) : (
                <IntegrationStatus
                  currentState={currentState}
                  i18nPublished={i18nPublished}
                  i18nUnpublished={i18nUnpublished}
                  i18nError={i18nError}
                />
              )}
            </DataListCell>,

            <DataListCell
              key={3}
              className={'integration-list-item__additional-info'}
              width={3}
            >
              {isConfigurationRequired && (
                <div
                  className={'integration-list-item__config-required'}
                  data-testid={`integrations-list-item-config-required`}
                >
                  <div className={'integration-list-item__text-wrapper'}>
                    <WarningTriangleIcon
                      color={global_warning_color_100.value}
                      size={'sm'}
                    />
                    &nbsp;
                    {i18nConfigurationRequired}
                  </div>
                </div>
              )}
            </DataListCell>,
          ]}
        />
        <DataListAction
          id={`integration-list-item-${id}-actions`}
          aria-label={`${integrationName} actions`}
          aria-labelledby={`integration-list-item-${id}-title`}
        >
          {actions}
        </DataListAction>
      </DataListItemRow>
    </DataListItem>
  );
};
