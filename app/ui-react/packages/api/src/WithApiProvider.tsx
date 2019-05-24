import { APISummary } from '@syndesis/models';
import * as React from 'react';
import { IFetchState } from './Fetch';
import { SyndesisFetch } from './SyndesisFetch';

export interface IWithApiProviderProps {
  specification: string;
  children(props: IFetchState<APISummary>): any;
}

export class WithApiProvider extends React.Component<IWithApiProviderProps> {
  public render() {
    const multipartFormData = new FormData();
    multipartFormData.append('specification', this.props.specification);
    return (
      <SyndesisFetch<APISummary>
        url={`/apis/info`}
        method={'POST'}
        includeContentType={false}
        contentType={'application/json; charset=utf-8'}
        body={multipartFormData}
        defaultValue={{
          name: '',
        }}
      >
        {({ response }) => this.props.children(response)}
      </SyndesisFetch>
    );
  }
}
