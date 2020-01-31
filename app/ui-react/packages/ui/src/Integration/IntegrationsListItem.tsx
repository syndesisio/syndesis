import {
  DataListAction,
  DataListCell,
  DataListItem,
  DataListItemCells,
  DataListItemRow,
  Text,
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
  return (
    <>
      <DataListItem
        data-testid={`integrations-list-item-${toValidHtmlId(
          integrationName
        )}-list-item`}
        className={'integration-list-item'}
        aria-labelledby={'integrationName'}
      >
        <DataListItemRow>
          {checkboxComponent ? checkboxComponent : null}
          <DataListItemCells
            dataListCells={[
              <DataListCell key={0}>
                <IntegrationIcon
                  startConnectionIcon={startConnectionIcon}
                  finishConnectionIcon={finishConnectionIcon}
                />
              </DataListCell>,
              <DataListCell key={1}>
                <Text component={'h3'}>{integrationName}</Text>
              </DataListCell>,
              <DataListCell
                key={2}
                className={'integration-list-item__additional-info'}
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
              >
                {isConfigurationRequired && (
                  <div
                    className={'integration-list-item__config-required'}
                    data-testid={`integrations-list-item-config-required`}
                  >
                    <WarningTriangleIcon
                      color={global_warning_color_100.value}
                      size={'sm'}
                    />
                    &nbsp;
                    {i18nConfigurationRequired}
                  </div>
                )}
              </DataListCell>,
            ]}
          />
          <DataListAction id={''} aria-label={''} aria-labelledby={''}>
            {actions}
          </DataListAction>
        </DataListItemRow>
      </DataListItem>
    </>
  );
};
