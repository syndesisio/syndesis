import { Step } from '@syndesis/models';
import {
  IntegrationStepsHorizontalItem,
  IntegrationStepsHorizontalView,
} from '@syndesis/ui';
import * as React from 'react';

export interface IIntegrationDetailStepsProps {
  steps: Step[];
}

export class IntegrationDetailSteps extends React.Component<
  IIntegrationDetailStepsProps
> {
  public render() {
    return (
      <IntegrationStepsHorizontalView>
        {this.props.steps.map((s, idx) => {
          const isFirst = idx === 0;
          const stepName = s.connection!
            ? s.connection!.connector!.name
            : s.name;

          return (
            <React.Fragment key={idx}>
              <IntegrationStepsHorizontalItem
                name={stepName}
                isFirst={isFirst}
              />
            </React.Fragment>
          );
        })}
      </IntegrationStepsHorizontalView>
    );
  }
}
