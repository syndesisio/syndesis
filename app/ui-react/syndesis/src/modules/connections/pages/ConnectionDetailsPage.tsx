import { WithConnection, WithConnectionHelpers } from '@syndesis/api';
import { Connection } from '@syndesis/models';
import { Breadcrumb, ConnectionDetailsHeader, Loader } from '@syndesis/ui';
import { WithLoader, WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { Link } from 'react-router-dom';
// import i18n from '../../../i18n';
import resolvers from '../../resolvers';

export interface IConnectionDetailsRouteParams {
  connectionId: string;
}

export interface IConnectionDetailsRouteState {
  connection?: Connection;
}

export default class ConnectionDetailsPage extends React.Component {
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
                const validate async () => {
                  await validateConfiguration();
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
                          <Link to={resolvers.connections.connections()}>
                            {t('shared:Connections')}
                          </Link>
                          <span>{t('connectionDetailPageTitle')}</span>
                        </Breadcrumb>
                        <ConnectionDetailsHeader>Detail goes here</ConnectionDetailsHeader>
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
