import * as React from 'react';
import { AuthContext } from '../auth';
import { IRestState, Rest } from './Rest';
import { Stream } from './Stream';


export interface ISynRest {
  autoload?: boolean;
  contentType?: string;
  poll?: number;
  url: string;
  stream?: boolean;
  children(props: IRestState): any;
}

export class SyndesisRest extends React.Component<ISynRest> {
  public render() {
    const { url, stream, ...props } = this.props;

    const RestOrStream = stream ? Stream : Rest;

    return (
      <AuthContext.Consumer>
        {({ token }) => (
          <RestOrStream
            baseUrl={'http://syndesis-server-syndesis.192.168.64.16.nip.io'}
            url={url}
            { ...props}
            headers={{
              'SYNDESIS-XSRF-TOKEN': 'awesome',
              'X-Forwarded-Access-Token': `${token}`,
              'X-Forwarded-User': 'admin'
            }}
          />
        )}
      </AuthContext.Consumer>
    )
  }
}
