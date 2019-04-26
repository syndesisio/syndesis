import { getConnectionIcon, WithConnectionHelpers } from '@syndesis/api';
import { Connection } from '@syndesis/models';
import {
  ConnectionCard,
  ConnectionsGrid,
  ConnectionsGridCell,
  ConnectionSkeleton,
} from '@syndesis/ui';
import { WithLoader } from '@syndesis/utils';
import * as H from 'history';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { AppContext } from '../../../app';
import { ApiError } from '../../../shared';

export interface IConnectionsProps {
  error: boolean;
  includeConnectionMenu: boolean;
  loading: boolean;
  connections: Connection[];
  getConnectionHref(connection: Connection): H.LocationDescriptor;
  getConnectionEditHref(connection: Connection): H.LocationDescriptor;
}

export class Connections extends React.Component<IConnectionsProps> {
  public render() {
    return (
      <AppContext.Consumer>
        {({ pushNotification }) => {
          return (
            <Translation ns={['connections', 'shared']}>
              {t => (
                <WithConnectionHelpers>
                  {({ deleteConnection }) => {
                    const doDelete = async (
                      connectionId: string,
                      connectionName: string
                    ) => {
                      try {
                        await deleteConnection(connectionId);
                        pushNotification(
                          t('connectionDeletedSuccess', { connectionName }),
                          'success'
                        );
                      } catch (error) {
                        const details = error.message ? error.message : '';
                        pushNotification(
                          t('connectionDeletedFailed', {
                            connectionName,
                            details,
                          }),
                          'error'
                        );
                      }
                    };
                    return (
                      <ConnectionsGrid>
                        <WithLoader
                          error={this.props.error}
                          loading={this.props.loading}
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
                            this.props.connections.map((c, index) => {
                              const handleDelete = (): void => {
                                doDelete(c.id!, c.name); // must have an ID if deleting
                              };

                              return (
                                <ConnectionsGridCell key={index}>
                                  <ConnectionCard
                                    name={c.name}
                                    description={c.description || ''}
                                    icon={getConnectionIcon(
                                      process.env.PUBLIC_URL,
                                      c,
                                    )}
                                    href={this.props.getConnectionHref(c)}
                                    menuProps={
                                      this.props.includeConnectionMenu
                                        ? {
                                            editHref: this.props.getConnectionEditHref(
                                              c
                                            ),
                                            i18nCancelLabel: t('shared:Cancel'),
                                            i18nDeleteLabel: t('shared:Delete'),
                                            i18nDeleteModalMessage: t(
                                              'deleteModalMessage',
                                              { connectionName: c.name }
                                            ),
                                            i18nDeleteModalTitle: t(
                                              'deleteModalTitle'
                                            ),
                                            i18nEditLabel: t('shared:Edit'),
                                            i18nMenuTitle: t(
                                              'connectionCardMenuTitle'
                                            ),
                                            i18nViewLabel: t('shared:View'),
                                            isDeleteEnabled:
                                              (c.uses as number) === 0,
                                            onDelete: handleDelete,
                                          }
                                        : undefined
                                    }
                                  />
                                </ConnectionsGridCell>
                              );
                            })
                          }
                        </WithLoader>
                      </ConnectionsGrid>
                    );
                  }}
                </WithConnectionHelpers>
              )}
            </Translation>
          );
        }}
      </AppContext.Consumer>
    );
  }
}
