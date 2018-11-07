import * as React from 'react';
import { IIntegrationsMetrics, IRestState, SyndesisRest } from './index';

export interface IWithIntegrationsMetricsProps {
  children(props: IRestState<IIntegrationsMetrics>): any;
}

export class WithIntegrationsMetrics extends React.Component<
  IWithIntegrationsMetricsProps
> {
  public render() {
    return (
      <SyndesisRest<IIntegrationsMetrics>
        url={'/api/v1/metrics/integrations'}
        poll={5000}
        defaultValue={{
          errors: 0,
          lastProcessed: 0,
          messages: 0,
          metricsProvider: '',
          start: 0,
          topIntegrations: {}
        }}
      >
        {response => this.props.children(response)}
      </SyndesisRest>
    );
  }
}
