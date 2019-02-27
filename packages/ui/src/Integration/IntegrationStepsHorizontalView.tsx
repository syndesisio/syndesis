import { Grid, Icon } from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationStepsHorizontalViewProps {
  value: string;
}

export class IntegrationStepsHorizontalView extends React.Component<
  IIntegrationStepsHorizontalViewProps
> {
  public render() {
    return (
      <Grid fluid={true} key={1}>
        {this.props.value ? (
          <Grid.Row className="show-grid">
            <Grid.Col xs={2} md={2}>
              <Icon name={'external-link'} />
            </Grid.Col>
            <Grid.Col xs={10} md={10}>
              <span>Hello</span>
            </Grid.Col>
          </Grid.Row>
        ) : null}

        {this.props.value ? (
          <Grid.Row className="show-grid">
            <Grid.Col xs={2} md={2}>
              {<span>{this.props.value}:</span>}
            </Grid.Col>
            <Grid.Col xs={10} md={10}>
              {this.props.value ? <span>Goodbye</span> : null}
            </Grid.Col>
          </Grid.Row>
        ) : null}
      </Grid>
    );
  }
}
