import { Card } from 'patternfly-react';
import * as React from 'react';

export class RecentUpdatesCard extends React.Component {
  public render() {
    return (
      <Card accented={false}>
        <Card.Heading>
          <Card.Title>Recent Updates</Card.Title>
        </Card.Heading>
        <Card.Body>{this.props.children}</Card.Body>
      </Card>
    );
  }
}
