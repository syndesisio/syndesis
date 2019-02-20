import { RestDataService } from '@syndesis/models';
import * as React from 'react';
import { DVFetch } from './DVFetch';
import { IFetchState } from './Fetch';
import { IChangeEvent } from './WithServerEvents';

export interface IWithVirtualizationsProps {
  children(props: IFetchState<RestDataService[]>): any;
}

export class WithVirtualizations extends React.Component<
  IWithVirtualizationsProps
> {
  public changeFilter(change: IChangeEvent) {
    return change.kind.startsWith('something goes here?');
  }

  public render() {
    return (
      <DVFetch<RestDataService[]>
        url={'workspace/dataservices'}
        defaultValue={[]}
      >
        {({ read, response }) => this.props.children(response)}
      </DVFetch>
    );
  }
}
