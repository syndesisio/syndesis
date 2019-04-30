import { getStepIcon, getSteps } from '@syndesis/api';
import { Integration } from '@syndesis/models';
import {
  IntegrationStepsHorizontalItem,
  IntegrationStepsHorizontalView,
} from '@syndesis/ui';
import * as React from 'react';

export interface IIntegrationDetailStepsProps {
  integration: Integration;
}

export class IntegrationDetailSteps extends React.Component<
  IIntegrationDetailStepsProps
> {
  public render() {
    const flowId = this.props.integration.flows![0].id!;
    const steps = getSteps(this.props.integration, flowId);

    return (
      <IntegrationStepsHorizontalView>
        {steps.map((s, idx) => {
          const isFirst = idx === 0;
          const stepName = s.connection!
            ? s.connection!.connector!.name
            : s.name;

          return (
            <React.Fragment key={idx}>
              <IntegrationStepsHorizontalItem
                name={stepName}
                icon={getStepIcon(
                  process.env.PUBLIC_URL,
                  this.props.integration,
                  flowId,
                  idx
                )}
                isFirst={isFirst}
              />
            </React.Fragment>
          );
        })}
      </IntegrationStepsHorizontalView>
    );
  }
}
