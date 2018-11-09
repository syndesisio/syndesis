import { IConnection, IIntegration, IIntegrationsMetrics, IMonitoredIntegration } from "@syndesis/models";
import { AggregatedMetricCard, ConnectionCard, ConnectionsGrid } from "@syndesis/ui";
import { getConnectionIcon } from "@syndesis/utils";
import { CardGrid, Grid } from 'patternfly-react';
import * as React from 'react';
import { Link } from 'react-router-dom';

// import './Dashboard.css';
import { ConnectionsMetric } from "./ConnectionsMetric";
import { IntegrationBoard } from "./IntegrationBoard";
import { RecentUpdates } from "./RecentUpdates";
import { TopIntegrations } from "./TopIntegrations";
import { UptimeMetric } from "./UptimeMetric";

export interface IIntegrationsPageProps {
  integrationsLoaded: boolean;
  connectionsLoaded: boolean;
  metricsLoaded: boolean;
  integrationsCount: number;
  integrationsErrorCount: number;
  connections: IConnection[];
  connectionsCount: number;
  metrics: IIntegrationsMetrics;
  topIntegrations: IMonitoredIntegration[];
  recentlyUpdatedIntegrations: IIntegration[];
  pendingIntegrations: number;
  runningIntegrations: number;
  stoppedIntegrations: number;
}

export class Dashboard extends React.Component<IIntegrationsPageProps> {
  public render() {
    return (
      <div className={'container-fluid'}>
        <Grid fluid={true}>
          <Grid.Row>
            <Grid.Col sm={12}>
              <div className={'Dashboard-header'}>
                <h1 className={'Dashboard-header__title'}>System metric</h1>
                <div className="Dashboard-header__actions">
                  <Link to={'/integrations'}>View All Integrations</Link>
                  <Link to={'/integrations/new'} className={'btn btn-primary'}>
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
              <AggregatedMetricCard
                title={`${this.props.integrationsCount} Integrations`}
                ok={
                  this.props.integrationsCount -
                  this.props.integrationsErrorCount
                }
                error={this.props.integrationsErrorCount}
              />
            </CardGrid.Col>
            <CardGrid.Col sm={6} md={3}>
              <ConnectionsMetric count={this.props.connectionsCount} />
            </CardGrid.Col>
            <CardGrid.Col sm={6} md={3}>
              <AggregatedMetricCard
                title={`${this.props.metrics.messages} Total Messages`}
                ok={this.props.metrics.messages - this.props.metrics.errors}
                error={this.props.metrics.errors}
              />
            </CardGrid.Col>
            <CardGrid.Col sm={6} md={3}>
              <UptimeMetric start={this.props.metrics.start} />
            </CardGrid.Col>
          </CardGrid.Row>
        </CardGrid>
        <Grid fluid={true}>
          <Grid.Row>
            <Grid.Col sm={12} md={6}>
              <TopIntegrations
                loading={!this.props.integrationsLoaded}
                topIntegrations={this.props.topIntegrations}
              />
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
                  <RecentUpdates
                    loading={!this.props.integrationsLoaded}
                    recentlyUpdatedIntegrations={
                      this.props.recentlyUpdatedIntegrations
                    }
                  />
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
                  <Link to={'/connections'}>View All Connections</Link>
                  <Link to={'/connections/new'} className={'btn btn-primary'}>
                    Create Connection
                  </Link>
                </div>
              </div>
            </Grid.Col>
          </Grid.Row>
        </Grid>
        <ConnectionsGrid loading={!this.props.connectionsLoaded}>
          {this.props.connections.map((c, index) => (
            <ConnectionCard
              name={c.name}
              description={c.description || ''}
              icon={getConnectionIcon(c, process.env.PUBLIC_URL)}
              key={index}
            />
          ))}
        </ConnectionsGrid>
      </div>
    );
  }
}
