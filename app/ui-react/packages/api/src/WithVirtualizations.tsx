import { RestDataService } from '@syndesis/models';
import * as React from 'react';
import { DVFetch } from './DVFetch';
import { IFetchState } from './Fetch';
import { WithPolling } from './WithPolling';

export interface IWithVirtualizationsProps {
  children(props: IFetchState<RestDataService[]>): any;
}

export class WithVirtualizations extends React.Component<
  IWithVirtualizationsProps
> {
  public render() {
    return (
      <DVFetch<RestDataService[]>
        url={'workspace/dataservices'}
        defaultValue={[]}
      >
        {({ read, response }) => {
          return (
            <WithPolling read={read} polling={5000}>
              {() => this.props.children(response)}
            </WithPolling>
          );
        }}
      </DVFetch>
    );
  }
}
