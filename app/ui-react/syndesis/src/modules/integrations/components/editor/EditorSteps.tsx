import { getConnectionIcon } from '@syndesis/api';
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
import { ApiError } from '../../../../shared';

export interface IEditorStepsProps {
  error: boolean;
  loading: boolean;
  connections: ConnectionOverview[];

  getConnectionHref(connection: ConnectionOverview): H.LocationDescriptor;
  getConnectionEditHref?(connection: ConnectionOverview): H.LocationDescriptor;
}

export class EditorSteps extends React.Component<IEditorStepsProps> {
  public render() {
    return (
      <Translation ns={['connections', 'shared']}>
        {t => (
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
                  const configurationRequired =
                    c.board &&
                    (c.board!.notices ||
                      c.board!.warnings ||
                      c.board!.errors)! > 0;

                  const isTechPreview =
                    c.connector! && c.connector!.metadata!
                      ? c.connector!.metadata!['tech-preview'] === 'true'
                      : false;

                  return (
                    <ConnectionsGridCell key={index}>
                      <ConnectionCard
                        name={c.name}
                        configurationRequired={configurationRequired}
                        description={c.description || ''}
                        icon={
                          // dirty hack to handle connection-like objects coming from the editor
                          c.icon && c.icon.includes(process.env.PUBLIC_URL)
                            ? c.icon
                            : getConnectionIcon(process.env.PUBLIC_URL, c)
                        }
                        href={this.props.getConnectionHref(c)}
                        i18nCannotDelete={t('cannotDelete')}
                        i18nConfigurationRequired={t('configurationRequired')}
                        i18nTechPreview={t('techPreview')}
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
        )}
      </Translation>
    );
  }
}
