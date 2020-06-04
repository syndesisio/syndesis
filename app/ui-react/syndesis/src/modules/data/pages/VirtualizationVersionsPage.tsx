import { useVirtualization, useVirtualizationEditions } from '@syndesis/api';
import { Virtualization, VirtualizationEdition } from '@syndesis/models';
import {
  IVirtualizationVersionItem,
  PageSection,
  VirtualizationListSkeleton,
  VirtualizationVersionsTable,
} from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import { WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { ApiError, PageTitle } from '../../../shared';
import {
  VirtualizationActionContainer,
  VirtualizationActionId,
} from '../shared/VirtualizationActionContainer';
import {
  IVirtualizationEditorPageRouteParams,
  IVirtualizationEditorPageRouteState,
  VirtualizationEditorPage,
} from './VirtualizationEditorPage';

const getDraftActions = (virtualization: Virtualization) => {
  const buttons = [VirtualizationActionId.Publish];
  const kebabItems = [VirtualizationActionId.Export];
  const draftActions = (
    <VirtualizationActionContainer
      includeActions={buttons}
      includeItems={kebabItems}
      virtualization={virtualization}
    />
  );
  return draftActions;
};

const getVersionActions = (virtualization: Virtualization, edition: number) => {
  const kebabItems =
    virtualization.deployedState === 'RUNNING' &&
    edition === virtualization.deployedRevision
      ? [VirtualizationActionId.Stop, VirtualizationActionId.Export]
      : [
          VirtualizationActionId.Start,
          VirtualizationActionId.Export,
          VirtualizationActionId.Revert,
        ];
  const versionActions = (
    <VirtualizationActionContainer
      includeActions={[]}
      includeItems={kebabItems}
      virtualization={virtualization}
      revision={edition}
    />
  );
  return versionActions;
};

const getSortedEditions = (
  editions: VirtualizationEdition[],
  virtualization: Virtualization
) => {
  const sorted = editions.sort((a, b) => {
    return b.revision - a.revision;
  });

  const versionItems: IVirtualizationVersionItem[] = [];
  for (const edition of sorted) {
    const versionItem: IVirtualizationVersionItem = {
      actions: getVersionActions(virtualization, edition.revision),
      publishedState: getVersionState(
        edition.revision,
        virtualization.deployedState,
        virtualization.publishedState,
        virtualization.deployedRevision,
        virtualization.publishedRevision
      ),
      timePublished: edition.createdAt
        ? new Date(edition.createdAt).toLocaleString()
        : '',
      version: edition.revision,
    };
    versionItems.push(versionItem);
  }
  return versionItems;
};

const getVersionState = (
  itemVersion: number,
  virtDeployedState: string,
  virtPublishedState: string,
  virtDeployedRevision?: number,
  virtPublishedRevision?: number
) => {
  // First consider the version deployed state
  let virtState: any = 'NOTFOUND';
  if (virtDeployedRevision && virtDeployedRevision === itemVersion) {
    if (virtDeployedState === 'RUNNING') {
      virtState = 'RUNNING';
    } else if (virtDeployedState === 'FAILED') {
      virtState = 'FAILED';
    } else if (virtDeployedState === 'DEPLOYING') {
      virtState = 'IN_PROGRESS';
    }
  }
  // If no deploy status found, consider the version publish state
  if (virtState === 'NOTFOUND' && virtPublishedRevision && virtPublishedRevision === itemVersion) {
    if (virtPublishedState === 'FAILED') {
      virtState = 'FAILED';
    } else if (virtPublishedState !== 'RUNNING') {
      virtState = 'IN_PROGRESS';
    }
  }
  return virtState;
};

/**
 * A page that displays virtualization publish state and version history.
 */
export const VirtualizationVersionsPage: React.FunctionComponent = () => {
  /**
   * Hook to obtain route params and history.
   */
  const { params, state } = useRouteData<
    IVirtualizationEditorPageRouteParams,
    IVirtualizationEditorPageRouteState
  >();

  /**
   * Hook to handle localization.
   */
  const { t } = useTranslation(['data', 'shared']);

  /**
   * Hook to obtain the virtualization being edited. Also does polling to get virtualization descriptor updates.
   */
  const { model: virtualization } = useVirtualization(params.virtualizationId);

  /**
   * Hook to obtain virtualization editions.
   */
  const { resource: editions, hasData, error } = useVirtualizationEditions(
    params.virtualizationId
  );

  const colHeaders = [
    t('versionsTableVersion'),
    t('versionsTablePublishedTime'),
    t('versionsTableRunning'),
    '',
  ];

  return (
    <VirtualizationEditorPage
      routeParams={params}
      routeState={state}
      virtualization={virtualization}
      items={[VirtualizationActionId.Stop, VirtualizationActionId.Delete]}
      actions={[VirtualizationActionId.Publish]}
      publishActionCustomProps={{ as: 'default' }}
    >
      <PageTitle title={t('versionsPageTitle')} />
      <PageSection>
        <WithLoader
          error={error !== false}
          loading={!hasData}
          loaderChildren={<VirtualizationListSkeleton width={800} />}
          errorChildren={<ApiError error={error as Error} />}
        >
          {() => (
            <VirtualizationVersionsTable
              a11yActionMenuColumn={t('actionsColumnA11yMessage')}
              isModified={virtualization.modified}
              i18nDraft={t('shared:Draft')}
              i18nEmptyVersionsTitle={t('versionsTableEmptyTitle')}
              i18nEmptyVersionsMsg={t('versionsTableEmptyMsg')}
              i18nPublish={t('shared:Publish')}
              draftActions={getDraftActions(virtualization)}
              tableHeaders={colHeaders}
              versionItems={getSortedEditions(editions, virtualization)}
            />
          )}
        </WithLoader>
      </PageSection>
    </VirtualizationEditorPage>
  );
};
