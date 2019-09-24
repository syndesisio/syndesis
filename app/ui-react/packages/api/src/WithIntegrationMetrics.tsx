import { IntegrationMetricsSummary } from '@syndesis/models';
import * as React from 'react';
import { IFetchState } from './Fetch';
import { SyndesisFetch } from './SyndesisFetch';
import { WithPolling } from './WithPolling';

export interface IWithIntegrationMetricsProps {
  disableUpdates?: boolean;
  integrationId: string;
  children(props: IFetchState<IntegrationMetricsSummary>): any;
}

export class WithIntegrationMetrics extends React.Component<
  IWithIntegrationMetricsProps
> {
  public render() {
    return (
      <SyndesisFetch<IntegrationMetricsSummary>
        url={`/metrics/integrations/${this.props.integrationId}`}
        defaultValue={{
          errors: 0, // int64
          lastProcessed: undefined, // date-time
          messages: 0, // int64
          metricsProvider: 'null',
          start: `${Date.now()}`, // date-time
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
