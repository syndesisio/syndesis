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
    return this.props.steps.map((s, idx) => {
      const isFirst = idx === 0;
      const stepName = s.connection!.connector!.name;

      return (
        <React.Fragment key={idx}>
          <IntegrationStepsHorizontalView name={stepName} isFirst={isFirst} />
        </React.Fragment>
      );
    });
  }
}
