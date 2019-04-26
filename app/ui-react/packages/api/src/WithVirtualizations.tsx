import { RestDataService } from '@syndesis/models';
import * as React from 'react';
import { DVFetch } from './DVFetch';
import { IFetchState } from './Fetch';
import { WithPolling } from './WithPolling';

export interface IWithVirtualizationsRenderProps
  extends IFetchState<RestDataService[]> {
  read(): Promise<void>;
}

export interface IWithVirtualizationsProps {
  children(props: IWithVirtualizationsRenderProps): any;
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
              {() => this.props.children({ ...response, read })}
            </WithPolling>
          );
        }}
      </DVFetch>
    );
  }
}
