import { Spinner } from 'patternfly-react';import * as React from 'react';
import {RestError} from "../ui";
import { IIntegration, SyndesisRest } from './index';


export interface IIntegrationsResponse {
  integrations: IIntegration[];
  totalCount: number;
}


export interface IWithProjectsProps {
  children(props: IIntegrationsResponse): any;
}

export class WithIntegrations extends React.Component<IWithProjectsProps> {
  public render() {
    return (
      <SyndesisRest url={'/api/v1/integrations'}>
        {({ loading, error, data }) => {
          if (loading) {
            return <Spinner/>;
          } else if (error) {
            return <RestError/>
          } else {
            return this.props.children({
              integrations: data.items,
              totalCount: data.totalCount
            });
          }
        }}
      </SyndesisRest>
    )
  }
}