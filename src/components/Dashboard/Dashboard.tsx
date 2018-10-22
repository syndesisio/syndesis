import { Button, CardGrid, Grid } from 'patternfly-react';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { IConnection, IIntegration, IIntegrationsMetrics, IMonitoredIntegration } from '../../containers';
import { AggregatedMetric } from './AggregatedMetric';
import { Connection } from './Connection';
import { ConnectionsMetric } from './ConnectionsMetric';
import { IntegrationBoard } from './IntegrationBoard';
import { RecentUpdates } from './RecentUpdates';
import { TopIntegrations } from './TopIntegrations';
import { UptimeMetric } from './UptimeMetric';

export interface IIntegrationsPageProps {
  monitoredIntegrations: IMonitoredIntegration[];
  integrationsCount: number;
  connections: IConnection[];
  connectionsCount: number;
  metrics: IIntegrationsMetrics;
}

export class Dashboard extends React.Component<IIntegrationsPageProps> {
  public render() {
    const {monitoredIntegrations, integrationsCount, metrics, connections, connectionsCount} = this.props;

    const getTimestamp = (integration: IIntegration) => {
      return integration.updatedAt !== 0 ? integration.updatedAt : integration.createdAt;
    };

    const byTimestamp = (a: IIntegration, b: IIntegration) => {
      const aTimestamp = getTimestamp(a);
      const bTimestamp = getTimestamp(b);
      return bTimestamp - aTimestamp;
    };

    const topIntegrations = metrics.topIntegrations || {};
    const topIntegrationsArray = Object.keys(topIntegrations).map(key => {
      return {
        count: topIntegrations[key],
        id: key,
      } as any;
    }).sort((a, b) => {
      return b.count - a.count;
    });
    const integrations = monitoredIntegrations.map(m => m.integration);
    const integrationsErrorCount = integrations.filter(i => i.currentState === 'Error').length;
    const sortedIntegrationsByTimestamp = integrations
      .sort(byTimestamp)
      .slice(0, 5);
    const sortedIntegrationsByMessageCount = monitoredIntegrations
      .sort((miA, miB) => byTimestamp(miA.integration, miB.integration))
      .sort((a, b) => {
        const index = topIntegrationsArray.findIndex(i => i.id === b.integration.id);
        return index === -1 ? topIntegrationsArray.length + 1 : index;
      })
      .reverse()
      .slice(0, 5);

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
                title={`${integrationsCount} Integrations`}
                ok={integrationsCount - integrationsErrorCount}
                error={integrationsErrorCount}
              />
            </CardGrid.Col>
            <CardGrid.Col sm={6} md={3}>
              <ConnectionsMetric count={connectionsCount}/>
            </CardGrid.Col>
            <CardGrid.Col sm={6} md={3}>
              <AggregatedMetric
                title={`${metrics.messages} Total Messages`}
                ok={metrics.messages - metrics.errors}
                error={metrics.errors}
              />
            </CardGrid.Col>
            <CardGrid.Col sm={6} md={3}>
              <UptimeMetric start={metrics.start}/>
            </CardGrid.Col>
          </CardGrid.Row>
        </CardGrid>
        <Grid fluid={true}>
          <Grid.Row>
            <Grid.Col sm={12} md={6}>
              <TopIntegrations integrations={sortedIntegrationsByMessageCount}/>
            </Grid.Col>
            <Grid.Col sm={12} md={6}>
              <Grid.Row>
                <Grid.Col sm={12}>
                  <IntegrationBoard integrations={integrations}/>
                </Grid.Col>
              </Grid.Row>
              <Grid.Row>
                <Grid.Col sm={12}>
                  <RecentUpdates integrations={sortedIntegrationsByTimestamp}/>
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
            {connections.map((c, index) =>
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