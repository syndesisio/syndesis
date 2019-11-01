import { useVirtualizationHelpers } from '@syndesis/api';
import {
  Virtualization,
  VirtualizationPublishingDetails,
} from '@syndesis/models';
import {
  PageSection,
  ViewHeaderBreadcrumb,
  VirtualizationDetailsHeader,
} from '@syndesis/ui';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { AppContext, UIContext } from '../../../app';
import resolvers from '../../resolvers';
import { VirtualizationNavBar } from '../shared';
import {
  getOdataUrl,
  getPublishingDetails,
  getStateLabelStyle,
  getStateLabelText,
  isPublishStep,
} from '../shared/VirtualizationUtils';
import './VirtualizationEditorPage.css';

/**
 * @param virtualizationName - the name of the virtualization whose details are being shown by this page
 */
export interface IVirtualizationEditorPageRouteParams {
  virtualizationId: string;
}

/**
 * @param virtualization - the virtualization whose details are being shown by this page
 */

export interface IVirtualizationEditorPageRouteState {
  virtualization: Virtualization;
}

export interface IVirtualizationEditorPageProps {
  /**
   * The route state.
   */
  routeState: IVirtualizationEditorPageRouteState;

  /**
   * The virtualization being edited.
   */
  virtualization: Virtualization;

  /**
   * The callback for when a virtualization has been successfully deleted.
   */
  onDeleteSuccess: () => void;
}

export const VirtualizationEditorPage: React.FunctionComponent<
  IVirtualizationEditorPageProps
> = props => {
  /**
   * Context that provides app-wide variables and functions.
   */
  const appContext = React.useContext(AppContext);

  /**
   * Context that broadcasts global notifications.
   */
  const { pushNotification } = React.useContext(UIContext);

  /**
   * Hook to handle localization.
   */
  const { t } = useTranslation(['data', 'shared']);

  /**
   * Hook that provides helper methods.
   */
  const {
    deleteVirtualization,
    exportVirtualization,
    publishVirtualization,
    unpublishVirtualization,
    updateVirtualizationDescription,
  } = useVirtualizationHelpers();

  // **********************
  // state
  // **********************
  const [currPublishedState, setCurrPublishedState] = React.useState(
    {} as VirtualizationPublishingDetails
  );
  const [isProgressWithLink, setProgressWithLink] = React.useState(false);
  const [isSubmitted, setSubmitted] = React.useState(false);
  const [labelType, setLabelType] = React.useState('default' as
    | 'danger'
    | 'primary'
    | 'default');
  const [publishStateText, setPublishStateText] = React.useState();
  const [description, setDescription] = React.useState(
    props.routeState.virtualization.description
  );

  /**
   * Update publishing details and description whenever a virtualization state changes.
   */
  React.useEffect(() => {
    const publishedDetails: VirtualizationPublishingDetails = getPublishingDetails(
      appContext.config.consoleUrl,
      props.virtualization
    ) as VirtualizationPublishingDetails;

    setCurrPublishedState(publishedDetails);
    setDescription(props.virtualization.description);
  }, [props.virtualization, appContext.config.consoleUrl]);

  /**
   * Update UI whenever publishing details change.
   */
  React.useEffect(() => {
    // turn off once publish/unpublish shows in-progress
    if (
      currPublishedState.state === 'DELETE_SUBMITTED' ||
      currPublishedState.state === 'SUBMITTED' ||
      isProgressWithLink
    ) {
      setSubmitted(false);
    }

    setProgressWithLink(isPublishStep(currPublishedState));

    if (!isSubmitted) {
      setLabelType(getStateLabelStyle(currPublishedState));
      setPublishStateText(getStateLabelText(currPublishedState));
    }
  }, [currPublishedState, isProgressWithLink, isSubmitted]);

  /**
   * Callback that deletes the virtualization.
   */
  const doDelete = async (): Promise<void> => {
    setSubmitted(true);

    // save current values in case we need to restore
    const saveText = publishStateText;
    const saveLabelType = labelType;

    setLabelType('default');
    setPublishStateText(t('deleteInProgress'));
    await deleteVirtualization(props.virtualization.name).catch((e: any) => {
      // inform user of error
      pushNotification(
        t('deleteVirtualizationFailed', {
          details: e.errorMessage || e.message || e,
          name: props.virtualization.name,
        }),
        'error'
      );

      // restore previous state
      setPublishStateText(saveText);
      setLabelType(saveLabelType);
      setSubmitted(false);
      throw e;
    });

    // successfully deleted so let page know
    props.onDeleteSuccess();
  };

  /**
   * Callback that exports the virtualization.
   */
  const doExport = () => {
    exportVirtualization(props.virtualization.name).catch((e: any) => {
      // notify user of error
      pushNotification(
        t('exportVirtualizationFailed', {
          details: e.errorMessage || e.message || e,
          name: props.virtualization.name,
        }),
        'error'
      );
    });
  };

  /**
   * Callback that publishes the virtualization.
   */
  const doPublish = async (): Promise<void> => {
    if (props.virtualization.empty) {
      pushNotification(
        t('publishVirtualizationNoViews', {
          name: props.virtualization.name,
        }),
        'info'
      );
      const e = new Error();
      e.name = 'NoViews';
      throw e;
    }

    setSubmitted(true);

    // save current values in case we need to restore
    const saveText = publishStateText;
    const saveLabelType = labelType;

    setLabelType('default');
    setPublishStateText(t('publishInProgress'));
    await publishVirtualization(props.virtualization.name).catch((e: any) => {
      pushNotification(
        t('publishVirtualizationFailed', {
          details: e.errorMessage || e.message || e,
          name: props.virtualization.name,
        }),
        'error'
      );

      // restore previous state
      setPublishStateText(saveText);
      setLabelType(saveLabelType);
      setSubmitted(false);
      throw e;
    });
  };

  /**
   * Updates the virtualization description.
   * @param newDescription the value of the description being set
   */
  const doSetDescription = async (newDescription: string) => {
    const previous = description;
    setDescription(newDescription); // this sets InlineTextEdit component to new value
    try {
      await updateVirtualizationDescription(
        props.virtualization.name,
        newDescription
      );
      return true;
    } catch {
      pushNotification(
        t('errorUpdatingDescription', {
          name: props.virtualization.name,
        }),
        'error'
      );
      setDescription(previous); // save failed so set InlineTextEdit back to old value
      return false;
    }
  };

  /**
   * Callback that will unpublish the virtualization.
   */
  const doUnpublish = async (): Promise<void> => {
    setSubmitted(true);

    // save current values in case we need to restore
    const saveText = publishStateText;
    const saveLabelType = labelType;

    setLabelType('default');
    setPublishStateText(t('unpublishInProgress'));
    await unpublishVirtualization(props.virtualization.name).catch((e: any) => {
      if (e.name === 'AlreadyUnpublished') {
        pushNotification(
          t('unpublishedVirtualization', {
            name: props.virtualization.name,
          }),
          'info'
        );
      } else {
        pushNotification(
          t('unpublishVirtualizationFailed', {
            details: e.errorMessage || e.message || e,
            name: props.virtualization.name,
          }),
          'error'
        );
      }

      // restore previous state
      setPublishStateText(saveText);
      setLabelType(saveLabelType);
      setSubmitted(false);
      throw e;
    });
  };

  /**
   *
   * @param integrationNames
   */
  const getUsedByMessage = (integrationNames: string[]): string => {
    if (integrationNames.length === 1) {
      return t('usedByOne');
    }

    return t('usedByMulti', { count: integrationNames.length });
  };

  return (
    <>
      <PageSection variant={'light'} noPadding={true}>
        <ViewHeaderBreadcrumb
          isSubmitted={isSubmitted}
          currentPublishedState={currPublishedState.state}
          virtualizationName={props.routeState.virtualization.name}
          dashboardHref={resolvers.dashboard.root()}
          dashboardString={t('shared:Home')}
          dataHref={resolvers.data.root()}
          dataString={t('shared:Virtualizations')}
          i18nCancelText={t('shared:Cancel')}
          i18nDelete={t('shared:Delete')}
          i18nDeleteModalMessage={t('deleteModalMessage', {
            name: props.routeState.virtualization.name,
          })}
          i18nDeleteModalTitle={t('deleteModalTitle')}
          i18nExport={t('shared:Export')}
          i18nPublish={t('shared:Publish')}
          i18nUnpublish={t('shared:Unpublish')}
          i18nUnpublishModalMessage={t('unpublishModalMessage', {
            name: props.routeState.virtualization.name,
          })}
          i18nUnpublishModalTitle={t('unpublishModalTitle')}
          onDelete={doDelete}
          onExport={doExport}
          onUnpublish={doUnpublish}
          onPublish={doPublish}
          usedInIntegration={props.virtualization.usedBy.length > 0}
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
          i18nInUseText={getUsedByMessage(props.virtualization.usedBy)}
          i18nPublishLogUrlText={t('shared:viewLogs')}
          odataUrl={getOdataUrl(props.virtualization)}
          publishedState={currPublishedState.state}
          publishingCurrentStep={currPublishedState.stepNumber}
          publishingLogUrl={currPublishedState.logUrl}
          publishingTotalSteps={currPublishedState.stepTotal}
          publishingStepText={currPublishedState.stepText}
          virtualizationDescription={description}
          virtualizationName={props.routeState.virtualization.name}
          isWorking={false}
          onChangeDescription={doSetDescription}
        />
      </PageSection>
      <PageSection variant={'light'} noPadding={true}>
        <VirtualizationNavBar virtualization={props.virtualization} />
      </PageSection>
      <PageSection variant={'light'} noPadding={true}>
        {props.children}
      </PageSection>
    </>
  );
};
