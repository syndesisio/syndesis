import { ProgressBar } from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationProgressProps {
  value: string;
  currentStep: number;
  totalSteps: number;
}

export class IntegrationProgress extends React.PureComponent<
  IIntegrationProgressProps
> {
  public render() {
    return (
      <div>
        <div>
          {this.props.value} ( {this.props.currentStep} /{' '}
          {this.props.totalSteps} )
        </div>
        <ProgressBar
          now={this.props.currentStep}
          max={this.props.totalSteps}
          style={{
            height: 6,
          }}
        />
      </div>
    );
  }
}
