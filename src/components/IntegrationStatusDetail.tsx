import { IIntegrationMonitoring } from '@syndesis/ui/containers';
import { Spinner } from 'patternfly-react';
import * as React from 'react';
import { IntegrationProgress } from './IntegrationProgress';

import './IntegrationStatusDetail.css'

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
    return (
      <div className={'integration-status-detail'}>
        {this.props.monitoring
          ? <IntegrationProgress monitoring={this.props.monitoring}/>
          : <>
            <Spinner loading={true} inline={true}/>
            {fallbackText}
          </>
        }
      </div>
    );
  }
}