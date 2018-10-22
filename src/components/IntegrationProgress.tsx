import { ProgressBar } from 'patternfly-react';
import * as React from 'react';
import { IIntegrationMonitoring } from '../containers';

export interface IIntegrationProgressProps {
  monitoring: IIntegrationMonitoring
}

export class IntegrationProgress extends React.Component<IIntegrationProgressProps> {
  public render() {
    const {value, currentStep, totalSteps} = this.props.monitoring.detailedState;
    return (
      <div>
        <div>{value} ( {currentStep} / {totalSteps} )</div>
        <ProgressBar
          now={currentStep}
          max={totalSteps}
          style={{
            height: 6
          }}
        />
      </div>
    );
  }
};