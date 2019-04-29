import { IApiVersion } from '@syndesis/models';
import * as React from 'react';
import { IFetchState } from './Fetch';
import { SyndesisFetch } from './SyndesisFetch';

export interface IWithApiVersionProps {
  children(props: IFetchState<IApiVersion>): any;
}

export class WithApiVersion extends React.Component<IWithApiVersionProps> {
  public render() {
    return (
      <SyndesisFetch<IApiVersion>
        url={`/version`}
        headers={{
          Accept: 'application/json',
        }}
        defaultValue={{
          'build-id': '',
          camelkruntimeversion: '',
          camelversion: '',
          'commit-id': '',
          version: '',
        }}
      >
        {({ response }) => this.props.children(response)}
      </SyndesisFetch>
    );
  }
}
