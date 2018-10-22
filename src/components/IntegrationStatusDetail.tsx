import { Spinner } from 'patternfly-react';
import * as React from 'react';
import { IIntegrationMonitoring } from '../containers';
import { IntegrationProgress } from './IntegrationProgress';


export interface IIntegrationStatusDetailProps {
  targetState: string;
  monitoring?: IIntegrationMonitoring;
}

export class IntegrationStatusDetail extends React.Component<IIntegrationStatusDetailProps> {
  public render() {
    let fallbackText = 'Pending';
    switch (this.props.targetState) {
      case 'Published':
        fallbackText = 'Starting...';
        break;
      case 'Unpublished':
        fallbackText = 'Stopping...';
        break;
    }
    return this.props.monitoring ? (
      <IntegrationProgress monitoring={this.props.monitoring}/>
    ) : (
      <div>
        <Spinner loading={true} inline={true}/>
        {fallbackText}
      </div>
    );
  }
}