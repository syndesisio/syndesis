import { CardGrid } from 'patternfly-react';
import * as React from 'react';

export class DvConnectionsGridCell extends React.Component {
  public render() {
    return (
      <CardGrid.Col xs={6} md={3}>
        {this.props.children}
      </CardGrid.Col>
    );
  }
}
