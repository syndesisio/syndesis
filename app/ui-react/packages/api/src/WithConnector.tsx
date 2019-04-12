import { Connector } from '@syndesis/models';
import * as React from 'react';
import { IFetchState } from './Fetch';
import { SyndesisFetch } from './SyndesisFetch';

export interface IWithConnectorProps {
  id: string;
  initialValue?: Connector;
  children(props: IFetchState<Connector>): any;
}

export class WithConnector extends React.Component<IWithConnectorProps> {
  public render() {
    return (
      <SyndesisFetch<Connector>
        url={`/connectors/${this.props.id}`}
        defaultValue={{
          actions: [],
          name: '',
        }}
        initialValue={this.props.initialValue}
      >
        {({ response }) => this.props.children(response)}
      </SyndesisFetch>
    );
  }
}
