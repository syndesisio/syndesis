import { IntegrationMetricsSummary } from '@syndesis/models';
import * as React from 'react';
import { IRestState } from './Rest';
import { SyndesisRest } from './SyndesisRest';

export interface IWithIntegrationsMetricsProps {
  children(props: IRestState<IntegrationMetricsSummary>): any;
}

export class WithIntegrationsMetrics extends React.Component<
  IWithIntegrationsMetricsProps
> {
  public render() {
    return (
      <SyndesisRest<IntegrationMetricsSummary>
        url={'/metrics/integrations'}
        poll={5000}
        defaultValue={{
          start: `${Date.now()}`, // date-time
          errors: 0, // int64
          messages: 0, // int64
          lastProcessed: `${Date.now()}`, // date-time
          metricsProvider: 'null',
          integrationDeploymentMetrics: [],
          topIntegrations: {},
          id: '-1',
        }}
      >
        {response => this.props.children(response)}
      </SyndesisRest>
    );
  }
}
