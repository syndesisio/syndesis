import { WithVirtualizations } from '@syndesis/api';
import { Connection } from '@syndesis/models';
import {
  ConnectionCard,
  ConnectionsGrid,
  ConnectionsGridCell,
  ConnectionSkeleton,
} from '@syndesis/ui';
import { WithLoader, WithRouter } from '@syndesis/utils';
import * as React from 'react';
import { Link } from 'react-router-dom';

export function getConnectionHref(
  baseUrl: string,
  connection: Connection
): string {
  return `${baseUrl}/${connection.id}`;
}

export default class VirtualizationsPage extends React.Component {
  public render() {
    return (
      <WithRouter>
        {({ match }) => (
          <WithVirtualizations>
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
                    data.items.map((c, index) => (
                      <ConnectionsGridCell key={index}>
                        <Link
                          to={`${match.url}/${c.keng_id}`}
                          style={{
                            color: 'inherit',
                            textDecoration: 'none',
                          }}
                        >
                          <ConnectionCard
                            name={c.keng_id}
                            description={c.tko_description || ''}
                            icon={'connection.png'}
                            href={getConnectionHref.bind(null, match.url)}
                          />
                        </Link>
                      </ConnectionsGridCell>
                    ))
                  }
                </WithLoader>
              </ConnectionsGrid>
            )}
          </WithVirtualizations>
        )}
      </WithRouter>
    );
  }
}
