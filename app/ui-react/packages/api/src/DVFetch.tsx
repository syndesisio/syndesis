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
        {({ apiUri, dvApiUri, headers }) => (
          <Fetch baseUrl={dvApiUri} url={url} headers={{}} {...props} />
        )}
      </ApiContext.Consumer>
    );
  }
}
