import * as React from 'react';
import { ApiContext } from './ApiContext';
import { IRestRenderProps, Rest } from './Rest';
import { Stream } from './Stream';

export interface ISyndesisRestProps<T> {
  autoload?: boolean;
  contentType?: string;
  url: string;
  stream?: boolean;
  defaultValue: T;
  children(props: IRestRenderProps<T>): any;
}

export class SyndesisRest<T> extends React.Component<ISyndesisRestProps<T>> {
  public render() {
    const { url, stream, ...props } = this.props;

    const RestOrStream = stream ? Stream : Rest;

    return (
      <ApiContext.Consumer>
        {({ apiUri, headers }) => (
          <RestOrStream
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
