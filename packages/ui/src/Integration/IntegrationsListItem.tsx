import { DropdownKebab, Icon, ListView, MenuItem } from 'patternfly-react';
import * as React from 'react';
import { IntegrationStatus } from './IntegrationStatus';
import { IntegrationStatusDetail } from './IntegrationStatusDetail';
import { IntegrationState } from './models';

export interface IIntegrationsListItemProps {
  integrationId: string;
  integrationName: string;
  currentState: IntegrationState;
  targetState: IntegrationState;
  isConfigurationRequired: boolean;
  monitoringValue?: string;
  monitoringCurrentStep?: number;
  monitoringTotalSteps?: number;
  monitoringLogUrl?: string;
  i18nConfigurationRequired: string;
  i18nError: string;
  i18nPublished: string;
  i18nProgressPending: string;
  i18nProgressStarting: string;
  i18nProgressStopping: string;
  i18nUnpublished: string;
  i18nLogUrlText: string;
}

export class IntegrationsListItem extends React.Component<
  IIntegrationsListItemProps
> {
  public render() {
    return (
      <ListView.Item
        actions={
          <div>
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
            <DropdownKebab
              id={`integration-${this.props.integrationId}-action-menu`}
              pullRight={true}
            >
              <MenuItem>Action 2</MenuItem>
            </DropdownKebab>
          </div>
        }
        heading={this.props.integrationName}
        description={
          this.props.isConfigurationRequired ? (
            <>
              <Icon type={'pf'} name={'warning-triangle-o'} />
              {this.props.i18nConfigurationRequired}
            </>
          ) : (
            ''
          )
        }
        hideCloseIcon={true}
        leftContent={<ListView.Icon name={'gear'} />}
        stacked={false}
      />
    );
  }
}
