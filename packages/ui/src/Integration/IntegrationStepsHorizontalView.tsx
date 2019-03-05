import { Grid, Icon } from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationStepsHorizontalViewProps {
  description?: string;
  steps?: any;
}

export class IntegrationStepsHorizontalView extends React.Component<
  IIntegrationStepsHorizontalViewProps
> {
  public render() {
    return (
      <>
        <Grid fluid={true}>
          <Grid.Row className="show-grid">
            <Grid.Col xs={6} md={4}>
              {this.props.steps && this.props.steps[0] ? (
                <>
                  <img src={this.props.steps[0].icon} />
                  <span>
                    <p>{this.props.steps[0].name}</p>
                  </span>
                </>
              ) : null}
            </Grid.Col>
            {this.props.steps &&
              this.props.steps.slice(1).map((opt: any, index: any) => (
                <>
                  <Grid.Col xs={6} md={4}>
                    <Icon name={'angle-right'} />
                  </Grid.Col>
                  <Grid.Col xsHidden={true} md={4}>
                    <span>
                      <img src={opt.icon} />
                      <p key={index}>{opt.name}</p>
                    </span>
                  </Grid.Col>
                </>
              ))}
          </Grid.Row>
        </Grid>
        <div>
          <p>{this.props.description}</p>
        </div>
      </>
    );
  }
}
