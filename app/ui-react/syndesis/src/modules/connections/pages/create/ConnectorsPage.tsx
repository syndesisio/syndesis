import { getConnectionIcon, WithConnectors } from '@syndesis/api';
import {
  ConnectionCard,
  ConnectionCreatorLayout,
  ConnectionsGrid,
  ConnectionsGridCell,
  ConnectionSkeleton,
} from '@syndesis/ui';
import { WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { ApiError, PageTitle } from '../../../../shared';
import { ConnectionCreatorBreadcrumbs } from '../../components';
import resolvers from '../../resolvers';

export default class ConnectorsPage extends React.Component {
  public render() {
    return (
      <WithConnectors>
        {({ data, hasData, error }) => (
          <>
            <PageTitle title={'Select connector'} />
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
                    errorChildren={<ApiError />}
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
                                process.env.PUBLIC_URL,
                                connector
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
          </>
        )}
      </WithConnectors>
    );
  }
}
