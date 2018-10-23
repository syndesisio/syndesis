import { Button, CardGrid, Grid } from 'patternfly-react';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { IConnection, IIntegrationsMetrics } from '../../containers';
import { AggregatedMetric } from './AggregatedMetric';
import { Connection } from './Connection';
import { ConnectionsMetric } from './ConnectionsMetric';
import { IIntegrationBoardProps, IntegrationBoard } from './IntegrationBoard';
import { IRecentUpdatesProps, RecentUpdates } from './RecentUpdates';
import { ITopIntegrationsProps, TopIntegrations } from './TopIntegrations';
import { UptimeMetric } from './UptimeMetric';

export interface IIntegrationsPageProps extends IIntegrationBoardProps, IRecentUpdatesProps, ITopIntegrationsProps {
  integrationsCount: number;
  integrationsErrorCount: number;
  connections: IConnection[];
  connectionsCount: number;
  metrics: IIntegrationsMetrics;
}

export class Dashboard extends React.Component<IIntegrationsPageProps> {
  public render() {
    return (
      <div className={'container-fluid'}>
        <Grid fluid={true}>
          <Grid.Row>
            <Grid.Col sm={6}>
              <h1>System metric</h1>
            </Grid.Col>
            <Grid.Col sm={6} className={'text-right'}>
              <Link to={'/integrations'}>View All Integrations</Link>
              <Button bsStyle={'primary'}>Create Integration</Button>
            </Grid.Col>
          </Grid.Row>
        </Grid>
        <CardGrid fluid={true} matchHeight={true}>
          <CardGrid.Row>
            <CardGrid.Col sm={6} md={3}>
              <AggregatedMetric
                title={`${this.props.integrationsCount} Integrations`}
                ok={this.props.integrationsCount - this.props.integrationsErrorCount}
                error={this.props.integrationsErrorCount}
              />
            </CardGrid.Col>
            <CardGrid.Col sm={6} md={3}>
              <ConnectionsMetric count={this.props.connectionsCount}/>
            </CardGrid.Col>
            <CardGrid.Col sm={6} md={3}>
              <AggregatedMetric
                title={`${this.props.metrics.messages} Total Messages`}
                ok={this.props.metrics.messages - this.props.metrics.errors}
                error={this.props.metrics.errors}
              />
            </CardGrid.Col>
            <CardGrid.Col sm={6} md={3}>
              <UptimeMetric start={this.props.metrics.start}/>
            </CardGrid.Col>
          </CardGrid.Row>
        </CardGrid>
        <Grid fluid={true}>
          <Grid.Row>
            <Grid.Col sm={12} md={6}>
              <TopIntegrations topIntegrations={this.props.topIntegrations}/>
            </Grid.Col>
            <Grid.Col sm={12} md={6}>
              <Grid.Row>
                <Grid.Col sm={12}>
                  <IntegrationBoard
                    runningIntegrations={this.props.runningIntegrations}
                    pendingIntegrations={this.props.pendingIntegrations}
                    stoppedIntegrations={this.props.stoppedIntegrations}
                  />
                </Grid.Col>
              </Grid.Row>
              <Grid.Row>
                <Grid.Col sm={12}>
                  <RecentUpdates recentlyUpdatedIntegrations={this.props.recentlyUpdatedIntegrations}/>
                </Grid.Col>
              </Grid.Row>
            </Grid.Col>
          </Grid.Row>
        </Grid>

        <Grid fluid={true} style={{marginTop: '20px'}}>
          <Grid.Row>
            <Grid.Col sm={6}>
              <h1>Connections</h1>
            </Grid.Col>
            <Grid.Col sm={6} className={'text-right'}>
              <a>View All Connections</a>
              <Button bsStyle={'primary'}>Create Connection</Button>
            </Grid.Col>
          </Grid.Row>
        </Grid>
        <CardGrid fluid={true} matchHeight={true}>
          <CardGrid.Row>
            {this.props.connections.map((c, index) =>
              <CardGrid.Col sm={6} md={3} key={index}>
                <Connection connection={c}/>
              </CardGrid.Col>
            )}
          </CardGrid.Row>
        </CardGrid>
      </div>
    );
  }
}