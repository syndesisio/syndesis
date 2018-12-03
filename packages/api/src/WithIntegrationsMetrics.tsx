import { IntegrationMetricsSummary } from '@syndesis/models';
import * as React from 'react';
import { IRestState } from './Rest';
import { SyndesisRest } from './SyndesisRest';
import { WithPolling } from './WithPolling';

export interface IWithIntegrationsMetricsProps {
  disableUpdates?: boolean;
  children(props: IRestState<IntegrationMetricsSummary>): any;
}

export class WithIntegrationsMetrics extends React.Component<
  IWithIntegrationsMetricsProps
> {
  public render() {
    return (
      <SyndesisRest<IntegrationMetricsSummary>
        url={'/metrics/integrations'}
        defaultValue={{
          errors: 0, // int64
          id: '-1',
          integrationDeploymentMetrics: [],
          lastProcessed: `${Date.now()}`, // date-time
          messages: 0, // int64
          metricsProvider: 'null',
          start: `${Date.now()}`, // date-time
          topIntegrations: {},
        }}
      >
        {({ read, response }) => {
          if (this.props.disableUpdates) {
            return this.props.children(response);
          }
          return (
            <WithPolling read={read} polling={5000}>
              {() => this.props.children(response)}
            </WithPolling>
          );
        }}
      </SyndesisRest>
    );
  }
}
