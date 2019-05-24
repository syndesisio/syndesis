import * as React from 'react';
import { ApiContext } from './ApiContext';
import { Fetch, IFetchProps, IFetchRenderProps } from './Fetch';
import { Stream } from './Stream';

type Omit<T, K> = Pick<T, Exclude<keyof T, K>>;

export interface ISyndesisFetchProps<T>
  extends Omit<IFetchProps<T>, 'baseUrl'> {
  autoload?: boolean;
  stream?: boolean;
  children(props: IFetchRenderProps<T>): any;
}

export class SyndesisFetch<T> extends React.Component<ISyndesisFetchProps<T>> {
  public render() {
    const { url, stream, ...props } = this.props;

    const FetchOrStream = stream ? Stream : Fetch;

    return (
      <ApiContext.Consumer>
        {({ apiUri, headers }) => {
          headers = {
            ...headers,
            ...(props.headers || {}),
          };
          return (
            <FetchOrStream
              baseUrl={apiUri}
              url={url}
              headers={headers}
              {...props}
            />
          );
        }}
      </ApiContext.Consumer>
    );
  }
}
