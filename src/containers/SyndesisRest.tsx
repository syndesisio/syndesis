import * as React from 'react';
import { AppContext } from '../app/AppContext';
import { AuthContext } from '../app/auth';
import { IRestState, Rest } from './Rest';
import { Stream } from './Stream';


export interface ISyndesisRestProps<T> {
  autoload?: boolean;
  contentType?: string;
  poll?: number;
  url: string;
  stream?: boolean;
  defaultValue: T;

  children(props: IRestState<T>): any;
}

export class SyndesisRest<T> extends React.Component<ISyndesisRestProps<T>> {
  public render() {
    const {url, stream, ...props} = this.props;

    const RestOrStream = stream ? Stream : Rest;

    return (
      <AppContext.Consumer>
        {({apiUri}) =>
          <AuthContext.Consumer>
            {({token}) => (
              <RestOrStream
                baseUrl={apiUri}
                url={url}
                {...props}
                headers={{
                  'SYNDESIS-XSRF-TOKEN': 'awesome',
                  'X-Forwarded-Access-Token': `${token}`,
                  'X-Forwarded-User': 'admin'
                }}
              />
            )}
          </AuthContext.Consumer>
        }
      </AppContext.Consumer>
    )
  }
}
