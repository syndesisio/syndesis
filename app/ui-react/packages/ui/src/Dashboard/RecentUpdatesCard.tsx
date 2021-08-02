import { Card, CardBody, CardTitle, Grid, Title } from '@patternfly/react-core';
import * as React from 'react';

export interface IRecentUpdatesProps {
  i18nTitle: string;
}

export class RecentUpdatesCard extends React.Component<IRecentUpdatesProps> {
  public render() {
    return (
      <Card data-testid={'dashboard-recent-updates'}>
        <CardTitle>
          <Title size="md" headingLevel="h2">
            {this.props.i18nTitle}
          </Title>
        </CardTitle>
        <CardBody>
          <br />
          <Grid hasGutter={true}>{this.props.children}</Grid>
        </CardBody>
      </Card>
    );
  }
}
