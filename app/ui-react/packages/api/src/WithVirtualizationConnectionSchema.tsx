import { SchemaNode } from '@syndesis/models';
import * as React from 'react';
import { DVFetch } from './DVFetch';
import { IFetchState } from './Fetch';

export interface IWithVirtualizationConnectionSchemaProps {
  connectionId: string;
  children(props: IFetchState<SchemaNode[]>): any;
}

export class WithVirtualizationConnectionSchema extends React.Component<
  IWithVirtualizationConnectionSchemaProps
> {
  public render() {
    return (
      <DVFetch<SchemaNode[]>
        url={`metadata/${this.props.connectionId}/schema`}
        defaultValue={[]}
      >
        {({ read, response }) => this.props.children(response)}
      </DVFetch>
    );
  }
}
