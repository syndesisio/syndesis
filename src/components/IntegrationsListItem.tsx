import { ListView, } from 'patternfly-react';
import * as React from 'react';
import { IMonitoredIntegration } from '../containers';
import { IntegrationStatus } from './IntegrationStatus';
import { IntegrationStatusDetail } from './IntegrationStatusDetail';

export interface IIntegrationsListItemProps {
  monitoredIntegration: IMonitoredIntegration;
}


export class IntegrationsListItem extends React.Component<IIntegrationsListItemProps> {
  public render() {
    const {integration, monitoring} = this.props.monitoredIntegration;
    return (
      <ListView.Item
        actions={<div/>}
        additionalInfo={[
          <ListView.InfoItem key={1}>
            {integration.currentState === 'Pending'
              ? <IntegrationStatusDetail
                targetState={integration.targetState}
                monitoring={monitoring}
              />
              : <IntegrationStatus integration={integration}/>
            }
          </ListView.InfoItem>,
        ]}
        heading={integration.name}
        hideCloseIcon={true}
        leftContent={<ListView.Icon name={'gear'}/>}
        stacked={true}
      />
    )
  }
}