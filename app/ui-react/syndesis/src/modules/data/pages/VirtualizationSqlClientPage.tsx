import {
  usePolling,
  useViewDefinitionDescriptors,
  useVirtualization,
  useVirtualizationHelpers,
} from '@syndesis/api';
import {
  Virtualization,
  VirtualizationPublishingDetails,
} from '@syndesis/models';
import {
  PageSection,
  SqlClientContentSkeleton,
  ViewHeaderBreadcrumb,
  VirtualizationDetailsHeader,
} from '@syndesis/ui';
import { useRouteData, WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { AppContext, UIContext } from '../../../app';
import { ApiError } from '../../../shared';
import resolvers from '../../resolvers';
import {
  VirtualizationNavBar,
  WithVirtualizationSqlClientForm,
} from '../shared';
import {
  getOdataUrl,
  getPublishingDetails,
  getStateLabelStyle,
  getStateLabelText,
  isPublishStep,
} from '../shared/VirtualizationUtils';
import './VirtualizationSqlClientPage.css';

/**
 * @param virtualizationId - the ID of the virtualization shown by this page.
 */
export interface IVirtualizationSqlClientPageRouteParams {
  virtualizationId: string;
}

/**
 * @param virtualization - the virtualization being shown by this page. If
 * exists, it must equal to the [virtualizationId]{@link IVirtualizationSqlClientPageRouteParams#virtualizationId}.
 */
export interface IVirtualizationSqlClientPageRouteState {
  virtualization: Virtualization;
}

/**
 * Page displays virtualization views and allows user run test queries against the views.
 */
export const VirtualizationSqlClientPage: React.FunctionComponent = () => {
  const { t } = useTranslation(['data', 'shared']);
  const { params, state, history } = useRouteData<
    IVirtualizationSqlClientPageRouteParams,
    IVirtualizationSqlClientPageRouteState
  >();
  const { resource: virtualization } = useVirtualization(
    params.virtualizationId
  );
  const [description, setDescription] = React.useState(
    state.virtualization.description
  );
  const appContext = React.useContext(AppContext);
  const { pushNotification } = React.useContext(UIContext);
  const {
    deleteVirtualization,
    exportVirtualization,
    publishVirtualization,
    unpublishVirtualization,
    updateVirtualizationDescription,
  } = useVirtualizationHelpers();
  const [currPublishedState, setCurrPublishedState] = React.useState(
    {} as VirtualizationPublishingDetails
  );
  const [prevPublishedState, setPrevPublishedState] = React.useState(
    {} as VirtualizationPublishingDetails
  );
  const [labelType, setLabelType] = React.useState('default' as 'danger' | 'primary' | 'default');
  const [publishStateText, setPublishStateText] = React.useState();
  const [usedBy, setUsedBy] = React.useState(state.virtualization.usedBy);

  const {
    resource: viewDefinitionDescriptors,
    error,
    loading,
  } = useViewDefinitionDescriptors(params.virtualizationId);

  const updatePublishedState = async () => {
    const publishedDetails: VirtualizationPublishingDetails = getPublishingDetails(
      appContext.config.consoleUrl,
      virtualization
    ) as VirtualizationPublishingDetails;

    setPrevPublishedState(currPublishedState);
    setCurrPublishedState(publishedDetails);
    setUsedBy(virtualization.usedBy);
  };

  // poll to check for updates to the published state
  usePolling({ callback: updatePublishedState, delay: 5000 });

  React.useEffect(() => {
    setLabelType(getStateLabelStyle(prevPublishedState, currPublishedState));
    setPublishStateText(getStateLabelText(prevPublishedState, currPublishedState));
  }, [currPublishedState, prevPublishedState]);

  const getUsedByMessage = (integrationNames: string[]): string => {
    if (integrationNames.length === 1) {
      return t('usedByOne');
    }

    return t('usedByMulti', { count: integrationNames.length });
  };

  const doDelete = async (virtId: string): Promise<string> => {
    // save current values in case we need to restore
    const saveText = publishStateText;
    const saveLabelType = labelType;

    setLabelType('default');
    setPublishStateText(t('deleteInProgress'));
    // manually set state here until polling returns
    const deleteSubmitted: VirtualizationPublishingDetails = {
      state: 'DELETE_SUBMITTED',
      stepNumber: 0,
      stepText: '',
      stepTotal: 0,
    };
    setPrevPublishedState(currPublishedState);
    setCurrPublishedState(deleteSubmitted);
    const result = await deleteVirtualization(virtId).catch((e: any) => {
      pushNotification(
        t('deleteVirtualizationFailed', {
          details: e.errorMessage || e.message || e,
          name: virtId,
        }),
        'error'
      );

      // restore previous state
      setPublishStateText(saveText);
      setLabelType(saveLabelType);
      setCurrPublishedState(prevPublishedState);
    });
    if (result) {
      // successfully deleted navigate to the virtualizations list page
      history.push(resolvers.data.virtualizations.list());
      return 'DELETED';
    }
    return 'FAILED';
  };

  const doExport = () => {
    exportVirtualization(virtualization.name).catch((e: any) => {
      // notify user of error
      pushNotification(
        t('exportVirtualizationFailed', {
          details: e.errorMessage || e.message || e,
          name: virtualization.name,
        }),
        'error'
      );
    });
  }

  const doPublish = async (
    virtId: string,
    hasViews: boolean
  ): Promise<string> => {
    if (!hasViews) {
      pushNotification(
        t('publishVirtualizationNoViews', {
          name: virtId,
        }),
        'info'
      );
      return 'FAILED';
    } else {
      // save current values in case we need to restore
      const saveText = publishStateText;
      const saveLabelType = labelType;

      setLabelType('default');
      setPublishStateText(t('publishInProgress'));
      // manually set state here until polling returns
      const submitted: VirtualizationPublishingDetails = {
        state: 'SUBMITTED',
        stepNumber: 0,
        stepText: '',
        stepTotal: 0,
      };
      setPrevPublishedState(currPublishedState);
      setCurrPublishedState(submitted);
      const teiidStatus = await publishVirtualization(virtId).catch(
        (e: any) => {
          pushNotification(
            t('publishVirtualizationFailed', {
              details: e.errorMessage || e.message || e,
              name: virtId,
            }),
            'error'
          );

          // restore previous state
          setPublishStateText(saveText);
          setLabelType(saveLabelType);
          setCurrPublishedState(prevPublishedState);
        }
      );
      if (teiidStatus) {
        if (teiidStatus.attributes['Build Status']) {
          return teiidStatus.attributes['Build Status'];
        }
        return 'SUBMITTED';
      }
      return 'FAILED';
    }
  };

  const doUnpublish = async (virtId: string): Promise<string> => {
    // save current values in case we need to restore
    const saveText = publishStateText;
    const saveLabelType = labelType;

    setLabelType('default');
    setPublishStateText(t('unpublishInProgress'));
    // manually set state here until polling returns
    const deleteSubmitted: VirtualizationPublishingDetails = {
      state: 'DELETE_SUBMITTED',
      stepNumber: 0,
      stepText: '',
      stepTotal: 0,
    };
    setPrevPublishedState(currPublishedState);
    setCurrPublishedState(deleteSubmitted);
    const buildStatus = await unpublishVirtualization(virtId).catch(
      (e: any) => {
        if (e.name === 'AlreadyUnpublished') {
          pushNotification(
            t('unpublishedVirtualization', {
              name: virtId,
            }),
            'info'
          );
        } else {
          pushNotification(
            t('unpublishVirtualizationFailed', {
              details: e.errorMessage || e.message || error,
              name: virtId,
            }),
            'error'
          );
        }

        // restore previous state
        setPublishStateText(saveText);
        setLabelType(saveLabelType);
        setCurrPublishedState(prevPublishedState);
      }
    );
    if (buildStatus) {
      if (buildStatus.status) {
        return buildStatus.status;
      }
      return 'DELETE_SUBMITTED';
    }
    return 'FAILED';
  };

  const doSetDescription = async (newDescription: string) => {
    const previous = description;
    setDescription(newDescription); // this sets InlineTextEdit component to new value
    try {
      await updateVirtualizationDescription(
        params.virtualizationId,
        newDescription
      );
      state.virtualization.description = newDescription;
      return true;
    } catch {
      pushNotification(
        t('errorUpdatingDescription', {
          name: state.virtualization.name,
        }),
        'error'
      );
      setDescription(previous); // save failed so set InlineTextEdit back to old value
      return false;
    }
  };

  const isProgressWithLink = isPublishStep(currPublishedState);

  return (
    <>
      <PageSection variant={'light'} noPadding={true}>
        <ViewHeaderBreadcrumb
          currentPublishedState={currPublishedState.state}
          virtualizationName={state.virtualization.name}
          dashboardHref={resolvers.dashboard.root()}
          dashboardString={t('shared:Home')}
          dataHref={resolvers.data.root()}
          dataString={t('shared:Virtualizations')}
          i18nCancelText={t('shared:Cancel')}
          i18nDelete={t('shared:Delete')}
          i18nDeleteModalMessage={t('deleteModalMessage', {
            name: state.virtualization.name,
          })}
          i18nDeleteModalTitle={t('deleteModalTitle')}
          i18nExport={t('shared:Export')}
          i18nPublish={t('shared:Publish')}
          i18nPublishInProgress={t('publishInProgress')}
          i18nUnpublish={t('shared:Unpublish')}
          i18nUnpublishInProgress={t('unpublishInProgress')}
          i18nUnpublishModalMessage={t('unpublishModalMessage', {
            name: state.virtualization.name,
          })}
          i18nUnpublishModalTitle={t('unpublishModalTitle')}
          onDelete={doDelete}
          onExport={doExport}
          onUnpublish={doUnpublish}
          onPublish={doPublish}
          hasViews={!state.virtualization.empty}
          usedInIntegration={usedBy.length > 0}
        />
      </PageSection>
      <PageSection
        className={'virtualization-sql-client-page'}
        variant={'light'}
        noPadding={true}
      >
        <VirtualizationDetailsHeader
          isProgressWithLink={isProgressWithLink}
          i18nPublishState={publishStateText}
          labelType={labelType}
          i18nDescriptionPlaceholder={t('descriptionPlaceholder')}
          i18nInUseText={getUsedByMessage(usedBy)}
          i18nPublishLogUrlText={t('shared:viewLogs')}
          odataUrl={getOdataUrl(virtualization)}
          publishedState={currPublishedState.state}
          publishingCurrentStep={currPublishedState.stepNumber}
          publishingLogUrl={currPublishedState.logUrl}
          publishingTotalSteps={currPublishedState.stepTotal}
          publishingStepText={currPublishedState.stepText}
          virtualizationDescription={description}
          virtualizationName={state.virtualization.name}
          isWorking={false}
          onChangeDescription={doSetDescription}
        />
      </PageSection>
      <PageSection variant={'light'} noPadding={true}>
        <VirtualizationNavBar virtualization={state.virtualization} />
      </PageSection>
      <PageSection variant={'light'} noPadding={true}>
        <WithLoader
          error={error !== false}
          loading={loading}
          loaderChildren={<SqlClientContentSkeleton />}
          errorChildren={<ApiError error={error as Error} />}
        >
          {() => (
            <WithVirtualizationSqlClientForm
              views={viewDefinitionDescriptors}
              virtualizationId={params.virtualizationId}
              linkCreateView={resolvers.data.virtualizations.create()}
              linkImportViews={resolvers.data.virtualizations.views.importSource.selectConnection(
                { virtualization: state.virtualization }
              )}
            >
              {() => <></>}
            </WithVirtualizationSqlClientForm>
          )}
        </WithLoader>
      </PageSection>
    </>
  );
};
