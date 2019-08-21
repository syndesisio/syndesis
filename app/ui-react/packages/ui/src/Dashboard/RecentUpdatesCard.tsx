import { Card, CardBody, CardHeader, Title } from '@patternfly/react-core';
import * as React from 'react';

export interface IRecentUpdatesProps {
  i18nTitle: string;
}

export class RecentUpdatesCard extends React.Component<IRecentUpdatesProps> {
  public render() {
    return (
      <Card data-testid={'dashboard-recent-updates'}>
        <CardHeader>
          <Title size="md" headingLevel="h2">
            {this.props.i18nTitle}
          </Title>
        </CardHeader>
        <CardBody>{this.props.children}</CardBody>
      </Card>
    );
  }
}
