import { WithConnectors } from '@syndesis/api';
import {
  ConnectionCard,
  ConnectionCreatorLayout,
  ConnectionsGrid,
  ConnectionsGridCell,
  ConnectionSkeleton,
} from '@syndesis/ui';
import { getConnectionIcon, WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { ConnectionCreatorBreadcrumbs } from '../../components';
import resolvers from '../../resolvers';

export default class ConnectorsPage extends React.Component {
  public render() {
    return (
      <WithConnectors>
        {({ data, hasData, error }) => (
          <ConnectionCreatorLayout
            header={<ConnectionCreatorBreadcrumbs step={1} />}
            content={
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
                    data.connectorsForDisplay
                      .sort((a, b) => a.name.localeCompare(b.name))
                      .map((connector, index) => (
                        <ConnectionsGridCell key={index}>
                          <ConnectionCard
                            name={connector.name}
                            description={connector.description || ''}
                            icon={getConnectionIcon(
                              connector,
                              process.env.PUBLIC_URL
                            )}
                            href={resolvers.create.configureConnector({
                              connector,
                            })}
                          />
                        </ConnectionsGridCell>
                      ))
                  }
                </WithLoader>
              </ConnectionsGrid>
            }
            cancelHref={resolvers.connections()}
          />
        )}
      </WithConnectors>
    );
  }
}
