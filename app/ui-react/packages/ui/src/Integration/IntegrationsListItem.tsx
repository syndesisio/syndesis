import { Icon, ListView } from 'patternfly-react';
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

export class IntegrationsListItem extends React.Component<
  IIntegrationsListItemProps
> {
  public render() {
    return (
      <ListView.Item
        data-testid={`integrations-list-item-${toValidHtmlId(
          this.props.integrationName
        )}-list-item`}
        checkboxInput={this.props.checkboxComponent || undefined}
        actions={this.props.actions}
        heading={this.props.integrationName}
        className={'integration-list-item'}
        additionalInfo={[
          <ListView.InfoItem
            key={1}
            className={'integration-list-item__additional-info'}
          >
            {this.props.currentState === 'Pending' ? (
              <IntegrationStatusDetail
                targetState={this.props.targetState}
                value={this.props.monitoringValue}
                currentStep={this.props.monitoringCurrentStep}
                totalSteps={this.props.monitoringTotalSteps}
                logUrl={this.props.monitoringLogUrl}
                i18nProgressPending={this.props.i18nProgressPending}
                i18nProgressStarting={this.props.i18nProgressStarting}
                i18nProgressStopping={this.props.i18nProgressStopping}
                i18nLogUrlText={this.props.i18nLogUrlText}
              />
            ) : (
              <IntegrationStatus
                currentState={this.props.currentState}
                i18nPublished={this.props.i18nPublished}
                i18nUnpublished={this.props.i18nUnpublished}
                i18nError={this.props.i18nError}
              />
            )}
          </ListView.InfoItem>,
          <ListView.InfoItem
            key={2}
            className={'integration-list-item__additional-info'}
          >
            {this.props.isConfigurationRequired && (
              <div
                className={'integration-list-item__config-required'}
                data-testid={`integrations-list-item-config-required`}
              >
                <Icon
                  type={'pf'}
                  name={'warning-triangle-o'}
                  className="pf-u-mr-xs"
                />
                {this.props.i18nConfigurationRequired}
              </div>
            )}
          </ListView.InfoItem>,
        ]}
        leftContent={
          <IntegrationIcon
            startConnectionIcon={this.props.startConnectionIcon}
            finishConnectionIcon={this.props.finishConnectionIcon}
          />
        }
        stacked={false}
      />
    );
  }
}
