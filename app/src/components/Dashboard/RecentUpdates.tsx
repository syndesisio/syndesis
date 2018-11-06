import { IntegrationStatus } from '@syndesis/ui/components';
import { IIntegration } from '@syndesis/ui/containers';
import { Card, Grid } from 'patternfly-react';
import * as React from 'react';
import { RecentUpdatesSkeleton } from './RecentUpdatsSkeleton';

export interface IRecentUpdatesProps {
  loading: boolean;
  recentlyUpdatedIntegrations: IIntegration[];
}

export class RecentUpdates extends React.Component<IRecentUpdatesProps> {
  public render() {
    return (
      <Card accented={false}>
        <Card.Heading>
          <Card.Title>
            Recent Updates
          </Card.Title>
        </Card.Heading>
        <Card.Body>
          {this.props.loading
            ? <RecentUpdatesSkeleton/>
            : (
              <Grid fluid={true}>
                {this.props.recentlyUpdatedIntegrations.map(i =>
                  <Grid.Row key={i.id}>
                    <Grid.Col sm={5}>
                      {i.name}
                    </Grid.Col>
                    <Grid.Col sm={3}>
                      <IntegrationStatus integration={i}/>
                    </Grid.Col>
                    <Grid.Col sm={4}>
                      {new Date(i.updatedAt || i.createdAt).toLocaleString()}
                    </Grid.Col>
                  </Grid.Row>
                )}
              </Grid>
            )}
        </Card.Body>
      </Card>
    );
  }
}