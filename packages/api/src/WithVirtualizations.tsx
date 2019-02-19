import { RestDataService } from '@syndesis/models';
import * as React from 'react';
import { DVFetch } from './DVFetch';
import { IFetchState } from './Fetch';
import { IChangeEvent } from './WithServerEvents';

export function getVirtualizationsForDisplay(
  virtualizations: RestDataService[]
) {
  return virtualizations;
}

export interface IVirtualizationsResponse {
  readonly items: RestDataService[];
}

export interface IWithVirtualizationsProps {
  disableUpdates?: boolean;
  children(props: IFetchState<IVirtualizationsResponse>): any;
}

export class WithVirtualizations extends React.Component<
  IWithVirtualizationsProps
> {
  public changeFilter(change: IChangeEvent) {
    return change.kind.startsWith('something goes here?');
  }

  public render() {
    return (
      <DVFetch<IVirtualizationsResponse>
        url={'workspace/dataservices'}
        defaultValue={{
          items: [],
        }}
      >
        {({ read, response }) => this.props.children(response)}
      </DVFetch>
    );
  }
}
