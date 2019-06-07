import { getMetadataValue } from '@syndesis/api';
import * as H from '@syndesis/history';
import {
  ConnectionCard,
  ConnectionsGrid,
  ConnectionsGridCell,
  ConnectionSkeleton,
} from '@syndesis/ui';
import { WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { ApiError, EntityIcon } from '../../../../shared';
import { IUIStep } from './interfaces';

export interface IEditorStepsProps {
  error: boolean;
  loading: boolean;
  steps: IUIStep[];

  getEditorStepHref(editorStep: IUIStep): H.LocationDescriptor;
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
              {() => {
                return this.props.steps.map((s, index) => {
                  const configurationRequired =
                    s.board &&
                    (s.board!.notices ||
                      s.board!.warnings ||
                      s.board!.errors)! > 0;

                  let isTechPreview = false;

                  if (s.connection && s.connection!.connector) {
                    isTechPreview =
                      getMetadataValue<string>(
                        'tech-preview',
                        s.connection.connector.metadata
                      ) === 'true';
                  } else if (s.connector) {
                    isTechPreview =
                      getMetadataValue<string>(
                        'tech-preview',
                        s.connector.metadata
                      ) === 'true';
                  }

                  return (
                    <ConnectionsGridCell key={index}>
                      <ConnectionCard
                        name={s.name}
                        configurationRequired={configurationRequired}
                        description={s.description || ''}
                        icon={<EntityIcon entity={s} alt={s.name} width={46} />}
                        href={this.props.getEditorStepHref(s)}
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
                });
              }}
            </WithLoader>
          </ConnectionsGrid>
        )}
      </Translation>
    );
  }
}
