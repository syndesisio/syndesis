import { Text } from '@patternfly/react-core';
import { Grid, Icon, ListViewIcon } from 'patternfly-react';
import * as React from 'react';

import './IntegrationStepsHorizontalView.css';

export interface IIntegrationSteps {
  name?: string;
}

export interface IIntegrationStepsHorizontalViewProps {
  steps: IIntegrationSteps[];
}

export class IntegrationStepsHorizontalView extends React.Component<
  IIntegrationStepsHorizontalViewProps
> {
  public render() {
    return (
      <div className="integration-steps-horizontal-view">
        <Grid fluid={true} xs={4}>
          <Grid.Row className="show-grid">
            <Grid.Col sm={6} md={4}>
              {this.props.steps && this.props.steps[0] ? (
                <>
                  <ListViewIcon name={'cube'} className="step-icon" />
                  <span>
                    <Text>{this.props.steps[0].name}</Text>
                  </span>
                </>
              ) : null}
            </Grid.Col>
            {this.props.steps &&
              this.props.steps.slice(1).map((opt: any, index: any) => (
                <div key={index}>
                  <Grid.Col sm={6} md={4}>
                    <Icon name={'angle-right'} className="step-arrow" />
                  </Grid.Col>
                  <Grid.Col xsHidden={true} md={4}>
                    <span>
                      <Icon name={'cube'} className="step-icon" />
                      <Text key={index}>{opt.name}</Text>
                    </span>
                  </Grid.Col>
                </div>
              ))}
          </Grid.Row>
        </Grid>
      </div>
    );
  }
}
