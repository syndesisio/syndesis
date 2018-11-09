import { IIntegration } from "@syndesis/models";
import * as React from 'react';
import { IRestState } from "./Rest";
import { SyndesisRest } from "./SyndesisRest";

export interface IIntegrationsResponse {
  items: IIntegration[];
  totalCount: number;
}

export interface IWithIntegrationsProps {
  children(props: IRestState<IIntegrationsResponse>): any;
}

export class WithIntegrations extends React.Component<IWithIntegrationsProps> {
  public render() {
    return (
      <SyndesisRest<IIntegrationsResponse>
        url={'/api/v1/integrations'}
        poll={5000}
        defaultValue={{
          items: [],
          totalCount: 0
        }}
      >
        {response => this.props.children(response)}
      </SyndesisRest>
    );
  }
}
