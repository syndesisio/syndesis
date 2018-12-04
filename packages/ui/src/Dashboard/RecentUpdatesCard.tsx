import { Card } from 'patternfly-react';
import * as React from 'react';

export interface IRecentUpdatesProps {
  i18nTitle: string;
}

export class RecentUpdatesCard extends React.Component<IRecentUpdatesProps> {
  public render() {
    return (
      <Card accented={false}>
        <Card.Heading>
          <Card.Title>{this.props.i18nTitle}</Card.Title>
        </Card.Heading>
        <Card.Body>{this.props.children}</Card.Body>
      </Card>
    );
  }
}
