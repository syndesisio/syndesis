import * as React from 'react';
import { IFetchState } from './Fetch';
import { SyndesisFetch } from './SyndesisFetch';
import { WithPolling } from './WithPolling';

// TODO this needs to be reconciled with the model
export interface IEnvironment {
  name: string;
  uses: number;
}

export type WithEnvironmentsResponse = Array<string | IEnvironment>;

export interface IWithEnvironmentsRenderProps
  extends IFetchState<WithEnvironmentsResponse> {
  read(): Promise<void>;
}

export interface IWithEnvironmentsProps {
  disableUpdates?: boolean;
  withUses?: boolean;
  children(props: IWithEnvironmentsRenderProps): any;
}

export class WithEnvironments extends React.Component<IWithEnvironmentsProps> {
  public render() {
    const url = this.props.withUses
      ? '/public/environments?withUses=true'
      : '/public/environments';
    return (
      <SyndesisFetch<WithEnvironmentsResponse> url={url} defaultValue={[]}>
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
