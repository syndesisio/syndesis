import { Spinner } from 'patternfly-react';
import * as React from 'react';
import { RestError } from '../ui';
import { IIntegrationsMetrics, SyndesisRest } from './index';

export interface IWithIntegrationsMetricsProps {
  children(props: IIntegrationsMetrics): any;
}

export class WithIntegrationsMetrics extends React.Component<IWithIntegrationsMetricsProps> {
  public render() {
    return (
      <SyndesisRest<IIntegrationsMetrics> url={'/api/v1/metrics/integrations'} poll={5000}>
        {({loading, error, data}) => {
          if (loading) {
            return <Spinner/>;
          } else if (error) {
            return <RestError/>
          } else {
            return this.props.children(data!);
          }
        }}
      </SyndesisRest>
    )
  }
}