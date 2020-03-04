import { useVirtualization, useVirtualizationEditions } from '@syndesis/api';
import { Virtualization, VirtualizationUserAction } from '@syndesis/models';
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

const getkebabItems = (virtualization: Virtualization, edition: number) => {
  const kebabItems =
    virtualization.deployedState === 'RUNNING' &&
    edition === virtualization.deployedRevision
      ? [VirtualizationActionId.Stop, VirtualizationActionId.Export]
      : [
          VirtualizationActionId.Start,
          VirtualizationActionId.Export,
          VirtualizationActionId.Revert,
        ];
  return kebabItems;
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
  if (
    virtState === 'NOTFOUND' &&
    virtPublishedRevision &&
    virtPublishedRevision === itemVersion
  ) {
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
   * State for showing Publishing... on virtualization publish clicked.
   */
  const [publishing, setPublishing] = React.useState<VirtualizationUserAction>({
    action: '',
    state: '',
    virtualizationName: '',
  });

  const setPublishingState = (
    uAction: string,
    uState: string,
    vName: string
  ) => {
    setPublishing({
      action: uAction,
      state: uState,
      virtualizationName: vName,
    });
  };

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
      versionPageAction={publishing}
    >
      <PageSection>
        <WithLoader
          error={error !== false}
          loading={!hasData}
          loaderChildren={<VirtualizationListSkeleton width={800} />}
          errorChildren={<ApiError error={error as Error} />}
        >
          {() => {
            const sorted = editions.sort(
              (a: { revision: number }, b: { revision: number }) => {
                return b.revision - a.revision;
              }
            );

            const versionItems: IVirtualizationVersionItem[] = [];
            for (const edition of sorted) {
              const versionItem: IVirtualizationVersionItem = {
                actions: (
                  <VirtualizationActionContainer
                    includeActions={[]}
                    includeItems={getkebabItems(
                      virtualization,
                      edition.revision
                    )}
                    virtualization={virtualization}
                    revision={edition.revision}
                    setPublishingState={setPublishingState}
                  />
                ),
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
            // return versionItems;
            return (
              <VirtualizationVersionsTable
                a11yActionMenuColumn={t('actionsColumnA11yMessage')}
                isModified={virtualization.modified}
                i18nDraft={t('shared:Draft')}
                i18nEmptyVersionsTitle={t('versionsTableEmptyTitle')}
                i18nEmptyVersionsMsg={t('versionsTableEmptyMsg')}
                i18nPublish={t('shared:Publish')}
                draftActions={
                  <VirtualizationActionContainer
                    includeActions={[VirtualizationActionId.Publish]}
                    includeItems={[VirtualizationActionId.Export]}
                    virtualization={virtualization}
                    setPublishingState={setPublishingState}
                  />
                }
                tableHeaders={colHeaders}
                versionItems={versionItems}
              />
            );
          }}
        </WithLoader>
      </PageSection>
    </VirtualizationEditorPage>
  );
};
