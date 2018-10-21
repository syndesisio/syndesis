import { Spinner } from 'patternfly-react';
import * as React from 'react';
import { RestError } from '../ui';
import { IIntegration, SyndesisRest } from './index';

export interface IIntegrationsResponse {
  integrations: IIntegration[];
  integrationsCount: number;
}

export interface IWithIntegrationsProps {
  children(props: IIntegrationsResponse): any;
}

export class WithIntegrations extends React.Component<IWithIntegrationsProps> {
  public render() {
    return (
      <SyndesisRest url={'/api/v1/integrations'} poll={1000}>
        {({loading, error, data}) => {
          if (loading) {
            return <Spinner/>;
          } else if (error) {
            return <RestError/>
          } else {
            return this.props.children({
              integrations: data.items,
              integrationsCount: data.totalCount
            });
          }
        }}
      </SyndesisRest>
    )
  }
}