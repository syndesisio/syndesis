import { useVirtualization, useVirtualizationEditions } from '@syndesis/api';
import { Virtualization, VirtualizationEdition } from '@syndesis/models';
import {
  IVirtualizationHistoryItem,
  PageSection,
  VirtualizationDetailHistoryTable,
  VirtualizationListSkeleton,
} from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import { WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { ApiError } from '../../../shared';
import {
  VirtualizationActionContainer,
  VirtualizationActionId,
} from '../shared/VirtualizationActionContainer';
import {
  IVirtualizationEditorPageRouteParams,
  IVirtualizationEditorPageRouteState,
  VirtualizationEditorPage,
} from './VirtualizationEditorPage';

const getVersionActions = (virtualization: Virtualization, edition: number) => {
  const kebabItems =
    virtualization.publishedState === 'RUNNING' &&
    edition === virtualization.publishedRevision
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

  const historyItems: IVirtualizationHistoryItem[] = [];
  for (const edition of sorted) {
    const historyItem: IVirtualizationHistoryItem = {
      actions: getVersionActions(virtualization, edition.revision),
      publishedState: getVersionPublishedState(
        edition.revision,
        virtualization.publishedState,
        virtualization.publishedRevision
      ),
      timePublished: edition.createdAt
        ? new Date(edition.createdAt).toLocaleString()
        : '',
      version: edition.revision,
    };
    historyItems.push(historyItem);
  }
  return historyItems;
};

const getVersionPublishedState = (
  itemVersion: number,
  virtPublishedState: string,
  virtPublishedRevision?: number
) => {
  if (virtPublishedRevision && virtPublishedRevision === itemVersion) {
    if (virtPublishedState === 'RUNNING') {
      return 'RUNNING';
    } else if (virtPublishedState === 'FAILED') {
      return 'FAILED';
    } else if (virtPublishedState === 'NOTFOUND') {
      return 'NOTFOUND';
    } else {
      return 'IN_PROGRESS';
    }
  }
  return 'NOTFOUND';
};

/**
 * A page that displays virtualization publish state and history.
 */
export const VirtualizationDetailsPage: React.FunctionComponent = () => {
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
    t('detailsVersionTableVersion'),
    t('detailsVersionTablePublishedTime'),
    t('detailsVersionTablePublished'),
    '',
  ];

  return (
    <VirtualizationEditorPage
      routeParams={params}
      routeState={state}
      virtualization={virtualization}
    >
      <PageSection>
        <WithLoader
          error={error !== false}
          loading={!hasData}
          loaderChildren={<VirtualizationListSkeleton width={800} />}
          errorChildren={<ApiError error={error as Error} />}
        >
          {() => (
            <VirtualizationDetailHistoryTable
              a11yActionMenuColumn={t('actionsColumnA11yMessage')}
              isModified={virtualization.modified}
              i18nEmptyVersionsTitle={t('detailsVersionTableEmptyTitle')}
              i18nEmptyVersionsMsg={t('detailsVersionTableEmptyMsg')}
              tableHeaders={colHeaders}
              historyItems={getSortedEditions(editions, virtualization)}
            />
          )}
        </WithLoader>
      </PageSection>
    </VirtualizationEditorPage>
  );
};
