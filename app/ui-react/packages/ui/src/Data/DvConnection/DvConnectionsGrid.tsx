import { CardGrid } from 'patternfly-react';
import * as React from 'react';

export class DvConnectionsGrid extends React.Component {
  public render() {
    return (
      <CardGrid fluid={true} matchHeight={true}>
        <CardGrid.Row>{this.props.children}</CardGrid.Row>
      </CardGrid>
    );
  }
}
