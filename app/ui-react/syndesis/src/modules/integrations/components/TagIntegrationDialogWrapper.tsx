import { WithEnvironments, WithIntegrationTags } from '@syndesis/api';
import {
  CiCdList,
  ITagIntegrationEntry,
  TagIntegrationDialog,
  TagIntegrationDialogEmptyState,
  TagIntegrationListItem,
} from '@syndesis/ui';
import * as React from 'react';
import { Translation } from 'react-i18next';

export interface ITagIntegrationDialogWrapperProps {
  manageCiCdHref: string;
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
  public render() {
    return (
      <Translation ns={['integrations', 'shared']}>
        {t => (
          <WithIntegrationTags integrationId={this.props.targetIntegrationId}>
            {({ data: tags, hasData: hasTags, error: tagError }) => (
              <WithEnvironments disableUpdates={true}>
                {({
                  data: environments,
                  hasData: hasEnvironments,
                  error: environmentsError,
                }) => {
                  const doSave = (items: ITagIntegrationEntry[]) => {
                    const newEnvironments = items
                      .filter(item => item.selected)
                      .map(item => item.name);
                    this.props.tagIntegration(
                      this.props.targetIntegrationId,
                      newEnvironments
                    );
                    this.props.onSave();
                  };
                  const mappedItems = environments.map(item => ({
                    name: item,
                    selected: typeof tags[item] !== 'undefined',
                  }));
                  return (
                    hasTags &&
                    hasEnvironments && (
                      <TagIntegrationDialog
                        i18nTitle={t('integrations:MarkIntegrationForCiCd')}
                        i18nCancelButtonText={t('shared:Cancel')}
                        i18nSaveButtonText={t('shared:Save')}
                        onHide={this.props.onHide}
                        onSave={doSave}
                        initialItems={mappedItems}
                      >
                        {({ handleChange, items }) => (
                          <>
                            {items.length > 0 && (
                              <>
                                <p>
                                  {t(
                                    'integrations:TagThisIntegrationForRelease'
                                  )}
                                </p>
                                <CiCdList
                                  children={mappedItems.map((item, index) => {
                                    return (
                                      <TagIntegrationListItem
                                        key={index}
                                        name={item.name}
                                        selected={item.selected}
                                        onChange={handleChange}
                                      />
                                    );
                                  })}
                                />
                              </>
                            )}
                            {items.length === 0 && (
                              <TagIntegrationDialogEmptyState
                                href={this.props.manageCiCdHref}
                                i18nTitle={t(
                                  'integrations:NoEnvironmentsAvailable'
                                )}
                                i18nInfo={''}
                                i18nGoToManageCiCdButtonText={t(
                                  'integrations:GoToManageCiCd'
                                )}
                              />
                            )}
                          </>
                        )}
                      </TagIntegrationDialog>
                    )
                  );
                }}
              </WithEnvironments>
            )}
          </WithIntegrationTags>
        )}
      </Translation>
    );
  }
}
