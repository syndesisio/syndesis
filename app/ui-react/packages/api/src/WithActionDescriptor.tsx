import { ActionDescriptor } from '@syndesis/models';
import * as React from 'react';
import { IFetchState } from './Fetch';
import { SyndesisFetch } from './SyndesisFetch';

export interface IWithActionDescriptorProps {
  connectionId: string;
  actionId: string;
  initialValue?: ActionDescriptor;
  configuredProperties?: { [name: string]: string };
  children(props: IFetchState<ActionDescriptor>): any;
}

export class WithActionDescriptor extends React.Component<
  IWithActionDescriptorProps
> {
  public render() {
    return (
      <SyndesisFetch<ActionDescriptor>
        url={`/connections/${this.props.connectionId}/actions/${
          this.props.actionId
        }`}
        defaultValue={{}}
        initialValue={this.props.initialValue}
        method={'POST'}
        body={this.props.configuredProperties || {}}
      >
        {({ response }) => this.props.children(response)}
      </SyndesisFetch>
    );
  }
}
