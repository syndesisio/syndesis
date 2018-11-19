import { Card, CardGrid, Grid } from 'patternfly-react';
import * as React from 'react';
import { Link } from 'react-router-dom';

import './Dashboard.css';

export interface IIntegrationsPageProps {
  linkToIntegrations: string;
  linkToIntegrationCreation: string;
  linkToConnections: string;
  linkToConnectionCreation: string;
  integrationsOverview: JSX.Element;
  connectionsOverview: JSX.Element;
  messagesOverview: JSX.Element;
  uptimeOverview: JSX.Element;
  topIntegrations: JSX.Element;
  integrationBoard: JSX.Element;
  integrationUpdates: JSX.Element;
  connections: JSX.Element;
}

export class Dashboard extends React.PureComponent<IIntegrationsPageProps> {
  public render() {
    return (
      <div className={'container-fluid'}>
        <Grid fluid={true}>
          <Grid.Row>
            <Grid.Col sm={12}>
              <div className={'Dashboard-header'}>
                <h1 className={'Dashboard-header__title'}>System metric</h1>
                <div className="Dashboard-header__actions">
                  <Link to={this.props.linkToIntegrations}>
                    View All Integrations
                  </Link>
                  <Link
                    to={this.props.linkToIntegrationCreation}
                    className={'btn btn-primary'}
                  >
                    Create Integration
                  </Link>
                </div>
              </div>
            </Grid.Col>
          </Grid.Row>
        </Grid>
        <CardGrid fluid={true} matchHeight={true}>
          <CardGrid.Row>
            <CardGrid.Col sm={6} md={3}>
              {this.props.integrationsOverview}
            </CardGrid.Col>
            <CardGrid.Col sm={6} md={3}>
              {this.props.connectionsOverview}
            </CardGrid.Col>
            <CardGrid.Col sm={6} md={3}>
              {this.props.messagesOverview}
            </CardGrid.Col>
            <CardGrid.Col sm={6} md={3}>
              {this.props.uptimeOverview}
            </CardGrid.Col>
          </CardGrid.Row>
        </CardGrid>
        <Grid fluid={true}>
          <Grid.Row>
            <Grid.Col sm={12} md={6}>
              {this.props.topIntegrations}
            </Grid.Col>
            <Grid.Col sm={12} md={6}>
              <Grid.Row>
                <Grid.Col sm={12}>{this.props.integrationBoard}</Grid.Col>
              </Grid.Row>
              <Grid.Row>
                <Grid.Col sm={12}>
                  <Card accented={false}>
                    <Card.Heading>
                      <Card.Title>Recent Updates</Card.Title>
                    </Card.Heading>
                    <Card.Body>{this.props.integrationUpdates}</Card.Body>
                  </Card>
                </Grid.Col>
              </Grid.Row>
            </Grid.Col>
          </Grid.Row>
        </Grid>

        <Grid fluid={true} style={{ marginTop: '20px' }}>
          <Grid.Row>
            <Grid.Col sm={12}>
              <div className={'Dashboard-header'}>
                <h1 className={'Dashboard-header__title'}>Connections</h1>
                <div className="Dashboard-header__actions">
                  <Link to={this.props.linkToConnections}>
                    View All Connections
                  </Link>
                  <Link
                    to={this.props.linkToConnectionCreation}
                    className={'btn btn-primary'}
                  >
                    Create Connection
                  </Link>
                </div>
              </div>
            </Grid.Col>
          </Grid.Row>
        </Grid>
        <CardGrid fluid={true} matchHeight={true}>
          <CardGrid.Row>{this.props.connections}</CardGrid.Row>
        </CardGrid>
      </div>
    );
  }
}
