import { DropdownKebab, Icon, ListView, MenuItem } from 'patternfly-react';
import * as React from 'react';
import { IntegrationStatus } from './IntegrationStatus';
import { IntegrationStatusDetail } from './IntegrationStatusDetail';

export interface IIntegrationsListItemProps {
  integrationId: string;
  integrationName: string;
  currentState: string;
  targetState: string;
  isConfigurationRequired: boolean;
  monitoringValue?: string;
  monitoringCurrentStep?: number;
  monitoringTotalSteps?: number;
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
              />
            ) : (
              <IntegrationStatus currentState={this.props.currentState} />
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
              Configuration Required
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
