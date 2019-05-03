import { getConnectionIcon, WithConnectionHelpers } from '@syndesis/api';
import * as H from '@syndesis/history';
import { ConnectionOverview } from '@syndesis/models';
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
import { ApiError } from '../../../shared';

export interface IConnectionsProps {
  error: boolean;
  includeConnectionMenu: boolean;
  loading: boolean;
  connections: ConnectionOverview[];
  getConnectionHref(connection: ConnectionOverview): H.LocationDescriptor;
  getConnectionEditHref(connection: ConnectionOverview): H.LocationDescriptor;
}

export class Connections extends React.Component<IConnectionsProps> {
  public render() {
    return (
      <UIContext.Consumer>
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

                              const configurationRequired =
                                (c.board!.notices ||
                                  c.board!.warnings ||
                                  c.board!.errors)! > 0;

                              const isTechPreview =
                                c.connector!.metadata! &&
                                c.connector!.metadata!['tech-preview'] ===
                                  'true';
                              const techPreviewPopoverHtml = (
                                <>
                                  <a
                                    href="https://access.redhat.com/support/offerings/techpreview"
                                    rel="nofollow"
                                    target="_blank"
                                    role="link"
                                  >
                                    Technology Previews
                                  </a>{' '}
                                  provide early access to features that are not
                                  yet supported. Feedback about these features
                                  is welcome. Send a message to{' '}
                                  <a href="mailto:fuse-online-tech-preview@redhat.com">
                                    fuse-online-tech-preview@redhat.com
                                  </a>
                                  .
                                </>
                              );

                              return (
                                <ConnectionsGridCell key={index}>
                                  <ConnectionCard
                                    name={c.name}
                                    configurationRequired={
                                      configurationRequired
                                    }
                                    description={c.description || ''}
                                    icon={
                                      // dirty hack to handle connection-like objects coming from the editor
                                      c.icon &&
                                      c.icon.includes(process.env.PUBLIC_URL)
                                        ? c.icon
                                        : getConnectionIcon(
                                            process.env.PUBLIC_URL,
                                            c
                                          )
                                    }
                                    href={this.props.getConnectionHref(c)}
                                    i18nConfigurationRequired={t(
                                      'configurationRequired'
                                    )}
                                    i18nTechPreview={t('techPreview')}
                                    menuProps={
                                      this.props.includeConnectionMenu
                                        ? {
                                            editHref: this.props
                                              .getConnectionEditHref!(c),
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
                                    techPreview={isTechPreview}
                                    techPreviewPopoverHtml={
                                      techPreviewPopoverHtml
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
      </UIContext.Consumer>
    );
  }
}
