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
  readonly connectionsForDisplay: RestDataService[];
  readonly connectionsWithToAction: RestDataService[];
  readonly connectionsWithFromAction: RestDataService[];
  readonly items: RestDataService[];
  readonly totalCount: number;
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
        url={'/vdb-builder/v1/workspace/dataservices'}
        defaultValue={{
          connectionsForDisplay: [],
          connectionsWithFromAction: [],
          connectionsWithToAction: [],
          items: [],
          totalCount: 0,
        }}
      >
        {({ read, response }) => {
          if (this.props.disableUpdates) {
            return this.props.children(response);
          }
        }}
      </DVFetch>
    );
  }
}
