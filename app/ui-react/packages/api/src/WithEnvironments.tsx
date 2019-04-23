import * as React from 'react';
import { IFetchState } from './Fetch';
import { SyndesisFetch } from './SyndesisFetch';
import { WithPolling } from './WithPolling';

export interface IWithEnvironmentsProps {
  disableUpdates?: boolean;
  children(props: IFetchState<string[]>): any;
}

export class WithEnvironments extends React.Component<IWithEnvironmentsProps> {
  public render() {
    return (
      <SyndesisFetch<string[]> url={'/public/environments'} defaultValue={[]}>
        {({ read, response }) => {
          if (this.props.disableUpdates) {
            return this.props.children(response);
          }
          return (
            <WithPolling read={read} polling={5000}>
              {() => this.props.children(response)}
            </WithPolling>
          );
        }}
      </SyndesisFetch>
    );
  }
}
