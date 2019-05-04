import { Grid, Icon } from 'patternfly-react';
import * as React from 'react';

import './IntegrationStepsHorizontalItem.css';

export interface IIntegrationStepsHorizontalItemProps {
  /**
   * The name of the connector used for the step.
   */
  name?: string;
  /**
   * The icon of the step.
   */
  icon?: string;
  /**
   * The boolean value that determines if the step
   * is the first in the steps array.
   */
  isFirst?: boolean;
}

export class IntegrationStepsHorizontalItem extends React.Component<
  IIntegrationStepsHorizontalItemProps
> {
  public render() {
    return (
      <div className="integration-steps-horizontal-item">
        {this.props.isFirst === false ? (
          <Grid.Col
            sm={1}
            md={1}
            className="integration-steps-horizontal-item__arrow"
          >
            <Icon name={'angle-right'} className="step-arrow" />
          </Grid.Col>
        ) : null}
        <Grid.Col
          sm={1}
          md={1}
          className="integration-steps-horizontal-item__icon"
        >
          <div className={'step-icon'}>
            <img src={this.props.icon} />
          </div>
          <p>{this.props.name}</p>
        </Grid.Col>
      </div>
    );
  }
}
