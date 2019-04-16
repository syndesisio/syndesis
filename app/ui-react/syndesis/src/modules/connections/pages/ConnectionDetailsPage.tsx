import { WithConnection, WithConnectionHelpers } from '@syndesis/api';
import { Connection } from '@syndesis/models';
import { Breadcrumb, ConnectionDetailsHeader, Loader } from '@syndesis/ui';
import { WithLoader, WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { Link } from 'react-router-dom';
import i18n from '../../../i18n';
import resolvers from '../../resolvers';

export interface IConnectionDetailsRouteParams {
  connectionId: string;
}

export interface IConnectionDetailsRouteState {
  connection?: Connection;
}

export default class ConnectionDetailsPage extends React.Component {
  public getUsedByMessage(connection: Connection): string {
    // TODO: Schema is currently wrong as it has 'uses' as an OptionalInt. Remove cast when schema is fixed.
    const numUsedBy = connection.uses as number;

    if (numUsedBy === 1) {
      return i18n.t('connections:usedByOne');
    }

    return i18n.t('connections:usedByMulti', { count: numUsedBy });
  }

  public saveDescription(newDescription: string) {
    // TODO: do something
  }

  public saveName(newName: string) {
    // TODO: do something
  }

  public render() {
    return (
      <WithRouteData<
        IConnectionDetailsRouteParams,
        IConnectionDetailsRouteState
      >>
        {({ connectionId }, { connection }, { history }) => {
          return (
            <WithConnectionHelpers>
              {({ validateConfiguration }) => {
                const validate = async (
                  theConnectorId: string,
                  theValues: { [key: string]: string }
                ) => {
                  await validateConfiguration(theConnectorId, theValues);
                };
                return (
                  <WithConnection id={connectionId} initialValue={connection}>
                    {({ data, hasData, error }) => (
                      <WithLoader
                        error={error}
                        loading={!hasData}
                        loaderChildren={<Loader />}
                        errorChildren={<div>TODO</div>}
                      >
                        {() => (
                          <Translation ns={['connections', 'shared']}>
                            {t => (
                              <>
                                <Breadcrumb>
                                  <Link to={resolvers.dashboard.root()}>
                                    {t('shared:Home')}
                                  </Link>
                                  <Link
                                    to={resolvers.connections.connections()}
                                  >
                                    {t('shared:Connections')}
                                  </Link>
                                  <span>{t('connectionDetailPageTitle')}</span>
                                </Breadcrumb>
                                <ConnectionDetailsHeader
                                  connectionDescription={data.description}
                                  connectionIcon={data.icon}
                                  connectionName={data.name}
                                  i18nDescriptionLabel={t('shared:Description')}
                                  i18nDescriptionPlaceholder={t(
                                    'descriptionPlaceholder'
                                  )}
                                  i18nIsRequiredMessage={t(
                                    'shared:requiredFieldMessage'
                                  )}
                                  i18nNamePlaceholder={t('namePlaceholder')}
                                  i18nUsageLabel={t('shared:Usage')}
                                  i18nUsageMessage={this.getUsedByMessage(data)}
                                  onSaveDescription={this.saveDescription}
                                  onSaveName={this.saveName}
                                />
                              </>
                            )}
                          </Translation>
                        )}
                      </WithLoader>
                    )}
                  </WithConnection>
                );
              }}
            </WithConnectionHelpers>
          );
        }}
      </WithRouteData>
    );
  }
}
