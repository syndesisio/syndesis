import { WithConnections } from '@syndesis/api';
import { Connection } from '@syndesis/models';
import { WithRouter } from '@syndesis/utils';
import * as React from 'react';
import { ConnectionsWithToolbar } from '../../connections/containers/ConnectionsWithToolbar';

export function getConnectionHref(
  baseUrl: string,
  connection: Connection
): string {
  return `${baseUrl}?cid=${connection.id}`;
}

export interface IIntegrationCreatorStep0PageState {}

export default class IntegrationCreatorStep0Page extends React.Component<
  {},
  IIntegrationCreatorStep0PageState
> {
  public state: IIntegrationCreatorStep0PageState = {};

  public render() {
    return (
      <WithRouter>
        {({ match }) => (
          <>
            <div className="container-fluid">
              <h1>Choose a Start Connection</h1>
              <p>
                Click the connection that starts the integration. If the
                connection you need is not available, click Create Connection.
              </p>
            </div>
            <WithConnections>
              {({ data, hasData, error }) => (
                <ConnectionsWithToolbar
                  error={error}
                  loading={!hasData}
                  connections={data.connectionsWithFromAction}
                  getConnectionHref={getConnectionHref.bind(null, match.url)}
                />
              )}
            </WithConnections>
          </>
        )}
      </WithRouter>
    );
  }
}
