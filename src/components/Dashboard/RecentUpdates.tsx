import { Card, Grid, Label } from 'patternfly-react';
import * as React from 'react';
import { IIntegration } from '../../containers';

export interface IRecentUpdatesProps {
  integrations: IIntegration[]
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
          <Grid fluid={true}>
            {this.props.integrations.map(i =>
              <Grid.Row key={i.id}>
                <Grid.Col sm={5}>
                  {i.name}
                </Grid.Col>
                <Grid.Col sm={3}>
                  <Label>{i.currentState}</Label>
                </Grid.Col>
                <Grid.Col sm={4}>
                  {new Date(i.updatedAt || i.createdAt).toLocaleString()}
                </Grid.Col>
              </Grid.Row>
            )}
          </Grid>
        </Card.Body>
      </Card>
    );
  }
}