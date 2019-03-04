import { Grid, Icon } from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationStepsHorizontalViewProps {
  // icon?: any;
  steps?: any;
}

export class IntegrationStepsHorizontalView extends React.Component<
  IIntegrationStepsHorizontalViewProps
> {
  public render() {
    return (
      <Grid fluid={true} key={1}>
        {this.props.steps &&
          this.props.steps.map((opt: any) => (
            <p key={opt.id}>blah, {opt.name}</p>
          ))}
        {this.props.steps ? (
          <Grid.Row className="show-grid">
            <Grid.Col xs={2} md={2}>
              <Icon name={'external-link'} />
            </Grid.Col>
            <Grid.Col xs={10} md={10}>
              <span>Hello:</span>
            </Grid.Col>
          </Grid.Row>
        ) : null}

        {this.props.steps ? (
          <Grid.Row className="show-grid">
            <Grid.Col xs={2} md={2}>
              {<span>Step:</span>}
            </Grid.Col>
            <Grid.Col xs={10} md={10}>
              {this.props.steps ? <span>Goodbye</span> : null}
            </Grid.Col>
          </Grid.Row>
        ) : null}
      </Grid>
    );
  }
}
