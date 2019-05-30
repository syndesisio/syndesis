import { getConnectionIcon, WithConnectors } from '@syndesis/api';
import { IConnectionWithIconFile } from '@syndesis/models';
import {
  ConnectionCard,
  ConnectionCreatorLayout,
  ConnectionsGrid,
  ConnectionsGridCell,
  ConnectionSkeleton,
  PageSection,
} from '@syndesis/ui';
import { WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { ApiError, PageTitle } from '../../../../shared';
import { ConnectionCreatorBreadSteps } from '../../components';
import { ConnectionCreatorBreadcrumb } from '../../components/ConnectionCreatorBreadcrumb';
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
                <ConnectionCreatorBreadcrumb
                  cancelHref={resolvers.connections()}
                />
                <ConnectionCreatorLayout
                  header={<ConnectionCreatorBreadSteps step={1} />}
                  content={
                    <PageSection>
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
                                  connector!.metadata!['tech-preview'] ===
                                    'true';

                                return (
                                  <ConnectionsGridCell key={index}>
                                    <ConnectionCard
                                      configurationRequired={false}
                                      name={connector.name}
                                      description={connector.description || ''}
                                      i18nCannotDelete={t('cannotDelete')}
                                      i18nConfigurationRequired={t(
                                        'configurationRequired'
                                      )}
                                      i18nTechPreview={t('techPreview')}
                                      icon={getConnectionIcon(
                                        process.env.PUBLIC_URL,
                                        connector as IConnectionWithIconFile
                                      )}
                                      href={resolvers.create.configureConnector(
                                        {
                                          connector,
                                        }
                                      )}
                                      techPreview={isTechPreview}
                                      techPreviewPopoverHtml={
                                        <span
                                          dangerouslySetInnerHTML={{
                                            __html: t('techPreviewPopoverHtml'),
                                          }}
                                        />
                                      }
                                    />
                                  </ConnectionsGridCell>
                                );
                              })
                          }
                        </WithLoader>
                      </ConnectionsGrid>
                    </PageSection>
                  }
                />
              </>
            )}
          </WithConnectors>
        )}
      </Translation>
    );
  }
}
