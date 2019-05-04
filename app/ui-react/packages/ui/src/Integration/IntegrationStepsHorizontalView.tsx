import { Grid } from 'patternfly-react';
import * as React from 'react';

export class IntegrationStepsHorizontalView extends React.Component {
  public render() {
    return (
      <Grid fluid={true} xs={1}>
        <Grid.Row className="show-grid">{this.props.children}</Grid.Row>
      </Grid>
    );
  }
}
