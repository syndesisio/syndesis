import * as React from 'react';
import { ApiContext } from './ApiContext';
import { Fetch, IFetchRenderProps } from './Fetch';

export interface IDVFetchProps<T> {
  autoload?: boolean;
  contentType?: string;
  url: string;
  stream?: boolean;
  defaultValue: T;
  initialValue?: T;
  children(props: IFetchRenderProps<T>): any;
}

export class DVFetch<T> extends React.Component<IDVFetchProps<T>> {
  public render() {
    const { url, stream, ...props } = this.props;

    return (
      <ApiContext.Consumer>
        {({ apiUri, headers }) => (
          <Fetch baseUrl={apiUri} url={url} headers={headers} {...props} />
        )}
      </ApiContext.Consumer>
    );
  }
}
