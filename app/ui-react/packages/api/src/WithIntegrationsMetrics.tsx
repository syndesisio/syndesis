import { IntegrationMetricsSummary } from '@syndesis/models';
import * as React from 'react';
import { IFetchState } from './Fetch';
import { SyndesisFetch } from './SyndesisFetch';
import { WithPolling } from './WithPolling';

export interface IWithIntegrationsMetricsProps {
  disableUpdates?: boolean;
  children(props: IFetchState<IntegrationMetricsSummary>): any;
}

export class WithIntegrationsMetrics extends React.Component<
  IWithIntegrationsMetricsProps
> {
  public render() {
    return (
      <SyndesisFetch<IntegrationMetricsSummary>
        url={'/metrics/integrations'}
        defaultValue={{
          errors: 0, // int64
          id: '-1',
          integrationDeploymentMetrics: [],
          lastProcessed: undefined, // date-time
          messages: 0, // int64
          metricsProvider: 'null',
          start: `${Date.now()}`, // date-time
          topIntegrations: {},
          uptimeDuration: 0, // int64
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
      </SyndesisFetch>
    );
  }
}
