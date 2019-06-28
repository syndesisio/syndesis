import { WithConnections } from '@syndesis/api';
import { SimplePageHeader } from '@syndesis/ui';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { PageTitle } from '../../../shared';
import { ConnectionsWithToolbar } from '../components';
import resolvers from '../resolvers';

export class ConnectionsPage extends React.Component {
  public render() {
    return (
      <Translation ns={['connections', 'shared']}>
        {t => (
          <WithConnections>
            {({ data, hasData, error, errorMessage }) => (
              <>
                <PageTitle title={'Connections'} />
                <SimplePageHeader
                  i18nTitle={t('shared:Connections')}
                  i18nDescription={t('connectionListDescription')}
                />
                <ConnectionsWithToolbar
                  error={error}
                  errorMessage={errorMessage}
                  includeConnectionMenu={true}
                  loading={!hasData}
                  connections={data.connectionsForDisplay}
                  getConnectionHref={connection =>
                    resolvers.connection.details({ connection })
                  }
                  getConnectionEditHref={connection =>
                    resolvers.connection.edit({ connection })
                  }
                />
              </>
            )}
          </WithConnections>
        )}
      </Translation>
    );
  }
}
