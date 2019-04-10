import { WithConnectors } from '@syndesis/api';
import { Connection } from '@syndesis/models';
import {
  ConnectionCard,
  ConnectionsGrid,
  ConnectionsGridCell,
  ConnectionSkeleton,
} from '@syndesis/ui';
import { getConnectionIcon, WithLoader } from '@syndesis/utils';
import * as React from 'react';
import resolvers from '../resolvers';

export function getConnectionHref(
  baseUrl: string,
  connection: Connection
): string {
  return `${baseUrl}/${connection.id}`;
}

export default class ConnectorsPage extends React.Component {
  public render() {
    return (
      <WithConnectors>
        {({ data, hasData, error }) => (
          <ConnectionsGrid>
            <WithLoader
              error={error}
              loading={!hasData}
              loaderChildren={
                <>
                  {new Array(5).fill(0).map((_, index) => (
                    <ConnectionsGridCell key={index}>
                      <ConnectionSkeleton />
                    </ConnectionsGridCell>
                  ))}
                </>
              }
              errorChildren={<div>TODO</div>}
            >
              {() =>
                data.items.map((connector, index) => (
                  <ConnectionsGridCell key={index}>
                    <ConnectionCard
                      name={connector.name}
                      description={connector.description || ''}
                      icon={getConnectionIcon(
                        connector,
                        process.env.PUBLIC_URL
                      )}
                      href={resolvers.create.configureConnector({ connector })}
                    />
                  </ConnectionsGridCell>
                ))
              }
            </WithLoader>
          </ConnectionsGrid>
        )}
      </WithConnectors>
    );
  }
}
