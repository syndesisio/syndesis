import { getConnectionIcon, WithConnectors } from '@syndesis/api';
import { IConnectionWithIconFile } from '@syndesis/models';
import {
  ConnectionCard,
  ConnectionCreatorLayout,
  ConnectionsGrid,
  ConnectionsGridCell,
  ConnectionSkeleton,
} from '@syndesis/ui';
import { WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { ApiError, PageTitle } from '../../../../shared';
import { ConnectionCreatorBreadcrumbs } from '../../components';
import resolvers from '../../resolvers';

export default class ConnectorsPage extends React.Component {
  public render() {
    return (
      <Translation ns={['connections', 'shared']}>
        {t => (
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
                            .map((connector, index) => {
                              const isTechPreview =
                                connector!.metadata! &&
                                connector!.metadata!['tech-preview'] === 'true';

                              return (
                                <ConnectionsGridCell key={index}>
                                  <ConnectionCard
                                    name={connector.name}
                                    description={connector.description || ''}
                                    i18nTechPreview={t('techPreview')}
                                    icon={getConnectionIcon(
                                      process.env.PUBLIC_URL,
                                      connector as IConnectionWithIconFile
                                    )}
                                    href={resolvers.create.configureConnector({
                                      connector,
                                    })}
                                    techPreview={isTechPreview}
                                  />
                                </ConnectionsGridCell>
                              );
                            })
                        }
                      </WithLoader>
                    </ConnectionsGrid>
                  }
                  cancelHref={resolvers.connections()}
                />
              </>
            )}
          </WithConnectors>
        )}
      </Translation>
    );
  }
}
