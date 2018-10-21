import { Button, CardGrid, Grid } from 'patternfly-react';
import * as React from 'react';
import { IConnection, IIntegration, IIntegrationsMetrics } from '../../containers';
import { AggregatedMetric } from './AggregatedMetric';
import { Connection } from './Connection';
import { ConnectionsMetric } from './ConnectionsMetric';
import { TopIntegrations } from './TopIntegrations';
import { UptimeMetric } from './UptimeMetric';

export interface IIntegrationsPageProps {
  integrations: IIntegration[];
  integrationsCount: number;
  connections: IConnection[];
  connectionsCount: number;
  metrics: IIntegrationsMetrics;
}

export class Dashboard extends React.Component<IIntegrationsPageProps> {
  public render() {
    const {integrations, integrationsCount, metrics, connections, connectionsCount} = this.props;
    const integrationsErrorCount = integrations.filter(i => i.currentState === 'Error').length;
    return (
      <div className={'container-fluid'}>
        <Grid fluid={true}>
          <Grid.Row>
            <Grid.Col sm={6}>
              <h1>System metric</h1>
            </Grid.Col>
            <Grid.Col sm={6}>
              <a>View All Integrations</a>
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
        <CardGrid fluid={true}>
          <CardGrid.Row>
            <CardGrid.Col sm={12} md={6}>
              <TopIntegrations integrations={integrations}/>
            </CardGrid.Col>
          </CardGrid.Row>
        </CardGrid>

        <Grid fluid={true} style={{marginTop: '20px'}}>
          <Grid.Row>
            <Grid.Col sm={6}>
              <h1>Connections</h1>
            </Grid.Col>
            <Grid.Col sm={6}>
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