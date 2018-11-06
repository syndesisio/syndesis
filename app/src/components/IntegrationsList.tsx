import { IMonitoredIntegration } from '@syndesis/app/containers';
import { ListView, } from 'patternfly-react';
import * as React from 'react';
import { IntegrationsListItem } from './IntegrationsListItem';

export interface IIntegrationsListProps {
  monitoredIntegrations: IMonitoredIntegration[];
}


export class IntegrationsList extends React.Component<IIntegrationsListProps> {
  public render() {
    return (
      <ListView>
        {this.props.monitoredIntegrations.map((integration: IMonitoredIntegration, index) => (
          <IntegrationsListItem
            monitoredIntegration={integration}
            key={index}
          />
        ))}
      </ListView>
    )
  }
}