import { CardGrid } from 'patternfly-react';
import * as React from 'react';

export class ConnectionsGridCell extends React.Component {
  public render() {
    return (
      <CardGrid.Col xs={2} sm={3} md={4} lg={5}>
        {this.props.children}
      </CardGrid.Col>
    );
  }
}
