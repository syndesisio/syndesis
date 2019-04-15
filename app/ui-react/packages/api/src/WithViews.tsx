import { RestViewDefinition } from '@syndesis/models';
import * as React from 'react';
import { DVFetch } from './DVFetch';
import { IFetchState } from './Fetch';

export interface IWithViewProps {
  vdbId: string;
  initialValue?: RestViewDefinition[];
  children(props: IFetchState<RestViewDefinition[]>): any;
}

/**
 * A component that fetches the specified VDB views.
 * @see [vdbId]{@link IWithViewProps#vdbId}
 */
export class WithViews extends React.Component<IWithViewProps> {
  public render() {
    return (
      <DVFetch<RestViewDefinition[]>
        url={`workspace/vdbs/${this.props.vdbId}/Models/views/Views`}
        defaultValue={[]}
        initialValue={this.props.initialValue}
      >
        {({ read, response }) => this.props.children(response)}
      </DVFetch>
    );
  }
}
