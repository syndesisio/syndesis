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
        {response => this.props.children(response)}
      </SyndesisRest>
    );
  }
}
