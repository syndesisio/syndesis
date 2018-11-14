import { WithConnectors } from '@syndesis/api';
import { ConnectionCard, ConnectionsGrid } from '@syndesis/ui';
import { getConnectionIcon, WithRouter } from '@syndesis/utils';
import * as React from 'react';
import { Link } from 'react-router-dom';

export default class ConnectorsPage extends React.Component {
  public render() {
    return (
      <WithRouter>
        {({ match }) => (
          <WithConnectors>
            {({ data, hasData, loading }) => (
              <div className={'container-fluid'}>
                <ConnectionsGrid loading={loading}>
                  {data.items.map((c, index) => (
                    <Link
                      to={`${match.url}/${c.id}`}
                      style={{ color: 'inherit', textDecoration: 'none' }}
                      key={index}
                    >
                      <ConnectionCard
                        name={c.name}
                        description={c.description || ''}
                        icon={getConnectionIcon(c, process.env.PUBLIC_URL)}
                      />
                    </Link>
                  ))}
                </ConnectionsGrid>
              </div>
            )}
          </WithConnectors>
        )}
      </WithRouter>
    );
  }
}
