import { IConnector } from "@syndesis/models";
import * as React from 'react';
import { IRestState } from "./Rest";
import { SyndesisRest } from "./SyndesisRest";

export interface IConnectorsResponse {
  items: IConnector[];
  totalCount: number;
}

export interface IWithConnectorsProps {
  children(props: IRestState<IConnectorsResponse>): any;
}

export class WithConnectors extends React.Component<IWithConnectorsProps> {
  public render() {
    return (
      <SyndesisRest<IConnectorsResponse>
        url={'/api/v1/connectors'}
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
