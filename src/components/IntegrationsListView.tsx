import * as React from 'react';
import { IIntegrationsMetrics, IMonitoredIntegration } from '../containers';
import { IntegrationsList } from './IntegrationsList';

export interface IIntegrationsListViewProps {
  monitoredIntegrations: IMonitoredIntegration[];
  integrationsCount: number;
  metrics: IIntegrationsMetrics;
}

export class IntegrationsListView extends React.Component<IIntegrationsListViewProps> {
  public render() {

    return (
      <div className={'container-fluid'}>
        <IntegrationsList monitoredIntegrations={this.props.monitoredIntegrations}/>
      </div>
    );
  }
}