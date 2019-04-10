import * as React from 'react';
import { ApiContext } from './ApiContext';
import { Fetch, IFetchRenderProps } from './Fetch';
import { Stream } from './Stream';

export interface ISyndesisFetchProps<T> {
  autoload?: boolean;
  contentType?: string;
  url: string;
  stream?: boolean;
  defaultValue: T;
  initialValue?: T;
  children(props: IFetchRenderProps<T>): any;
}

export class SyndesisFetch<T> extends React.Component<ISyndesisFetchProps<T>> {
  public render() {
    const { url, stream, ...props } = this.props;

    const FetchOrStream = stream ? Stream : Fetch;

    return (
      <ApiContext.Consumer>
        {({ apiUri, headers }) => (
          <FetchOrStream
            baseUrl={apiUri}
            url={url}
            headers={headers}
            {...props}
          />
        )}
      </ApiContext.Consumer>
    );
  }
}
