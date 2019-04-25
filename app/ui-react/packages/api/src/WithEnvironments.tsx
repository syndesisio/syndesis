import * as React from 'react';
import { IFetchState } from './Fetch';
import { SyndesisFetch } from './SyndesisFetch';
import { WithPolling } from './WithPolling';

export interface IWithEnvironmentsRenderProps extends IFetchState<string[]> {
  read(): Promise<void>;
}

export interface IWithEnvironmentsProps {
  disableUpdates?: boolean;
  children(props: IWithEnvironmentsRenderProps): any;
}

export class WithEnvironments extends React.Component<IWithEnvironmentsProps> {
  public render() {
    return (
      <SyndesisFetch<string[]> url={'/public/environments'} defaultValue={[]}>
        {({ read, response }) => {
          if (this.props.disableUpdates) {
            return this.props.children({ ...response, read });
          }
          return (
            <WithPolling read={read} polling={5000}>
              {() => this.props.children({ ...response, read })}
            </WithPolling>
          );
        }}
      </SyndesisFetch>
    );
  }
}
