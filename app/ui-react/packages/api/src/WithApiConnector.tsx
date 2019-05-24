import { Connector } from '@syndesis/models';
import * as React from 'react';
import { IFetchState } from './Fetch';
import { SyndesisFetch } from './SyndesisFetch';

export interface IWithApiConnectorProps {
  apiConnectorId: string;
  initialValue?: Connector;
  children(props: IFetchState<Connector>): any;
}

/**
 * A component that fetches the API Connector with the specified identifier.
 * @see [apiConnectorId]{@link IWithApiConnectorProps#apiConnectorId}
 */
export class WithApiConnector extends React.Component<IWithApiConnectorProps> {
  public render() {
    return (
      <SyndesisFetch<Connector>
        url={`/connectors/${this.props.apiConnectorId}`}
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
