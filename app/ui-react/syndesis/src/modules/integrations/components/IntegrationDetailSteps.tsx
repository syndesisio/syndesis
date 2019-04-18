import { Step } from '@syndesis/models';
import { IntegrationStepsHorizontalView } from '@syndesis/ui';
import * as React from 'react';

export interface IIntegrationDetailStepsProps {
  steps: Step[];
}

export class IntegrationDetailSteps extends React.Component<
  IIntegrationDetailStepsProps
> {
  public render() {
    return <IntegrationStepsHorizontalView steps={this.props.steps} />;
  }
}
