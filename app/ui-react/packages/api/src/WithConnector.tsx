import { IConnector } from '@syndesis/models';
import * as React from 'react';
import { IFetchState } from './Fetch';
import { SyndesisFetch } from './SyndesisFetch';

export interface IWithConnectorProps {
  id: string;
  initialValue?: IConnector;
  children(props: IFetchState<IConnector>): any;
}

export class WithConnector extends React.Component<IWithConnectorProps> {
  public render() {
    return (
      <SyndesisFetch<IConnector>
        url={`/connectors/${this.props.id}`}
        defaultValue={{
          actions: [],
          isTechPreview: false,
          name: '',
        }}
        initialValue={this.props.initialValue}
      >
        {({ response }) => this.props.children(response)}
      </SyndesisFetch>
    );
  }
}
