import { WithConnectionHelpers } from '@syndesis/api';
import * as H from '@syndesis/history';
import { IConnectionOverview } from '@syndesis/models';
import {
  ConnectionCard,
  ConnectionsGrid,
  ConnectionsGridCell,
  ConnectionSkeleton,
} from '@syndesis/ui';
import { WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { UIContext } from '../../../app';
import { ApiError, EntityIcon } from '../../../shared';

export interface IConnectionsProps {
  error: boolean;
  errorMessage?: string;
  includeConnectionMenu: boolean;
  loading: boolean;
  connections: IConnectionOverview[];

  getConnectionHref(connection: IConnectionOverview): H.LocationDescriptor;
  getConnectionEditHref?(connection: IConnectionOverview): H.LocationDescriptor;
}

export class Connections extends React.Component<IConnectionsProps> {
  public render() {
    return (
      <Translation ns={['connections', 'shared']}>
        {t => (
          <UIContext.Consumer>
            {({ pushNotification }) => {
              return (
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
                      <WithLoader
                        error={this.props.error}
                        loading={this.props.loading}
                        loaderChildren={
                          <ConnectionsGrid>
                            {new Array(5).fill(0).map((_, index) => (
                              <ConnectionsGridCell key={index}>
                                <ConnectionSkeleton />
                              </ConnectionsGridCell>
                            ))}
                          </ConnectionsGrid>
                        }
                        errorChildren={
                          <ApiError error={this.props.errorMessage!} />
                        }
                      >
                        {() => {
                          return (
                            <ConnectionsGrid data-testid={'connections'}>
                              {this.props.connections.map((c, index) => {
                                const handleDelete = (): void => {
                                  doDelete(c.id!, c.name); // must have an ID if deleting
                                };
                                return (
                                  <ConnectionsGridCell key={index}>
                                    <ConnectionCard
                                      name={c.name}
                                      description={c.description || ''}
                                      icon={
                                        <EntityIcon
                                          entity={c}
                                          alt={c.name}
                                          width={46}
                                        />
                                      }
                                      href={this.props.getConnectionHref(c)}
                                      i18nCannotDelete={t('cannotDelete')}
                                      i18nConfigRequired={t(
                                        'configurationRequired'
                                      )}
                                      i18nTechPreview={t('shared:techPreview')}
                                      isConfigRequired={c.isConfigRequired}
                                      isTechPreview={c.isTechPreview}
                                      menuProps={
                                        this.props.includeConnectionMenu
                                          ? {
                                              editHref: this.props
                                                .getConnectionEditHref!(c),
                                              i18nCancelLabel: t(
                                                'shared:Cancel'
                                              ),
                                              i18nDeleteLabel: t(
                                                'shared:Delete'
                                              ),
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
                                      techPreviewPopoverHtml={
                                        <span
                                          dangerouslySetInnerHTML={{
                                            __html: t(
                                              'shared:techPreviewPopoverHtml'
                                            ),
                                          }}
                                        />
                                      }
                                    />
                                  </ConnectionsGridCell>
                                );
                              })}
                            </ConnectionsGrid>
                          );
                        }}
                      </WithLoader>
                    );
                  }}
                </WithConnectionHelpers>
              );
            }}
          </UIContext.Consumer>
        )}
      </Translation>
    );
  }
}
