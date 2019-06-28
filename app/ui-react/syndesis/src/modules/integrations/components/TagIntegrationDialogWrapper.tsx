import { WithEnvironments, WithIntegrationTags } from '@syndesis/api';
import * as H from '@syndesis/history';
import {
  CiCdList,
  CiCdListSkeleton,
  ITagIntegrationEntry,
  TagIntegrationDialog,
  TagIntegrationDialogBody,
} from '@syndesis/ui';
import { WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { ApiError } from '../../../shared';
import './TagIntegrationDialogWrapper.css';

export interface ITagIntegrationDialogWrapperProps {
  manageCiCdHref: H.LocationDescriptor;
  targetIntegrationId: string;
  onSave: () => void;
  onHide: () => void;
  tagIntegration: (
    integrationId: string,
    environments: string[]
  ) => Promise<Response>;
}

export class TagIntegrationDialogWrapper extends React.Component<
  ITagIntegrationDialogWrapperProps
> {
  public constructor(props: ITagIntegrationDialogWrapperProps) {
    super(props);
    this.handleSave = this.handleSave.bind(this);
  }
  public handleSave(items: ITagIntegrationEntry[]) {
    this.props.onSave();
    const newEnvironments = items
      .filter(item => item.selected)
      .map(item => item.name);
    this.props.tagIntegration(this.props.targetIntegrationId, newEnvironments);
  }
  public render() {
    return (
      <Translation ns={['integrations', 'shared']}>
        {t => (
          <TagIntegrationDialog
            i18nTitle={t('integrations:MarkIntegrationForCiCd')}
            i18nCancelButtonText={t('shared:Cancel')}
            i18nSaveButtonText={t('shared:Save')}
            onHide={this.props.onHide}
            onSave={this.handleSave}
          >
            {({ handleChange }) => (
              <WithIntegrationTags
                integrationId={this.props.targetIntegrationId}
              >
                {({
                  data: tags,
                  hasData: hasTags,
                  error: tagError,
                  errorMessage: tagErrorMessage,
                }) => (
                  <WithEnvironments disableUpdates={true}>
                    {({
                      data: environments,
                      hasData: hasEnvironments,
                      error: environmentsError,
                    }) => {
                      return (
                        <>
                          <p className="tag-integration-dialog-wrapper__description">
                            {t('integrations:TagThisIntegrationForRelease')}
                          </p>
                          <WithLoader
                            error={tagError || environmentsError}
                            loading={!hasTags && !hasEnvironments}
                            loaderChildren={
                              <CiCdList>
                                <CiCdListSkeleton />
                              </CiCdList>
                            }
                            errorChildren={
                              <ApiError error={tagErrorMessage!} />
                            }
                          >
                            {() => {
                              const mappedItems = environments.map(item => ({
                                name: item as string,
                                selected:
                                  typeof tags[item as string] !== 'undefined',
                              }));
                              return (
                                <TagIntegrationDialogBody
                                  key={JSON.stringify(tags)}
                                  initialItems={mappedItems}
                                  onChange={handleChange}
                                  manageCiCdHref={this.props.manageCiCdHref}
                                  i18nEmptyStateTitle={t(
                                    'integrations:NoEnvironmentsAvailable'
                                  )}
                                  i18nEmptyStateInfo={t(
                                    'integrations:NoEnvironmentsAvailableInfo'
                                  )}
                                  i18nEmptyStateButtonText={t(
                                    'integrations:GoToManageCiCd'
                                  )}
                                />
                              );
                            }}
                          </WithLoader>
                        </>
                      );
                    }}
                  </WithEnvironments>
                )}
              </WithIntegrationTags>
            )}
          </TagIntegrationDialog>
        )}
      </Translation>
    );
  }
}
