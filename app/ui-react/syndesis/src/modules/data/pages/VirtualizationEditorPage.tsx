import { useVirtualizationHelpers } from '@syndesis/api';
import {
  Virtualization,
  VirtualizationPublishingDetails,
} from '@syndesis/models';
import {
  PageSection,
  VirtualizationBreadcrumb,
  VirtualizationDetailsHeader,
} from '@syndesis/ui';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { AppContext, UIContext } from '../../../app';
import resolvers from '../../resolvers';
import {
  VirtualizationActionContainer,
  VirtualizationActionId,
  VirtualizationNavBar,
} from '../shared';
import {
  getOdataUrl,
  getPublishingDetails,
  getStateLabelStyle,
  getStateLabelText,
  isPublishStep,
} from '../shared/VirtualizationUtils';
import './VirtualizationEditorPage.css';

/**
 * This will always have a value and can be used for the virtualization name.
 * @property {string} virtualizationId - the name of the virtualization whose details are being shown by this page
 */
export interface IVirtualizationEditorPageRouteParams {
  virtualizationId: string;
}

/**
 * This will *not* have a value if cutting and pasting a URL.
 * @property {Virtualization} virtualization - the virtualization whose details are being shown by this page
 */
export interface IVirtualizationEditorPageRouteState {
  virtualization: Virtualization;
}

export interface IVirtualizationEditorPageProps {
  /**
   * The breadcrumb button actions. Leave `undefined` if default actions are wanted.
   */
  actions?: VirtualizationActionId[];

  deleteActionCustomProps?: any;
  exportActionCustomProps?: any;
  publishActionCustomProps?: any;
  saveActionCustomProps?: any;
  startActionCustomProps?: any;
  stopActionCustomProps?: any;

  /**
   * The breadcrumb kebab menu items. Leave `undefined` if default kebab menu items are wanted.
   */
  items?: VirtualizationActionId[];

  /**
   * The route parameters.
   */
  routeParams: IVirtualizationEditorPageRouteParams;

  /**
   * The route state.
   */
  routeState: IVirtualizationEditorPageRouteState;

  /**
   * The virtualization being edited.
   */
  virtualization: Virtualization;
}

export const VirtualizationEditorPage: React.FunctionComponent<IVirtualizationEditorPageProps> = props => {
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
  const { updateVirtualizationDescription } = useVirtualizationHelpers();

  /**
   * State for the current published state.
   */
  const [currPublishedState, setCurrPublishedState] = React.useState(
    {} as VirtualizationPublishingDetails
  );

  /**
   * State for the virtualization description.
   */
  const [description, setDescription] = React.useState(() => {
    if (
      props.routeState.virtualization &&
      props.routeState.virtualization.description
    ) {
      return props.routeState.virtualization.description;
    }

    if (props.virtualization && props.virtualization.description) {
      return props.virtualization.description;
    }

    return '';
  });

  /**
   * State indicating if a published state is a step state.
   */
  const [isProgressWithLink, setProgressWithLink] = React.useState(false);

  /**
   * State indicating if an operation is in progress.
   */
  const [isSubmitted, setSubmitted] = React.useState(false);

  /**
   * State identifying the type that should be used by labels.
   */
  const [labelType, setLabelType] = React.useState(
    'default' as 'danger' | 'primary' | 'default'
  );

  /**
   * State for the user-friendly text of the current published state.
   */
  const [publishStateText, setPublishStateText] = React.useState(() => {
    if (props.routeState.virtualization) {
      return props.routeState.virtualization.publishedState;
    }

    if (props.virtualization) {
      return props.virtualization.publishedState;
    }

    return '';
  });

  /**
   * Update publishing details and description whenever a virtualization state changes.
   */
  React.useEffect(() => {
    const publishedDetails: VirtualizationPublishingDetails = getPublishingDetails(
      appContext.config.consoleUrl,
      props.virtualization.name
        ? props.virtualization
        : props.routeState.virtualization
    ) as VirtualizationPublishingDetails;

    setCurrPublishedState(publishedDetails);
    setDescription(props.virtualization.description);
  }, [
    props.routeState.virtualization,
    props.virtualization,
    appContext.config.consoleUrl,
  ]);

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
   * Using this method instead of using `description` directly prevented the description in the details
   * header from displaying the description placeholder initially.
   */
  const getDescription = () => {
    if (description) {
      return description;
    }

    if (props.virtualization && props.virtualization.description) {
      return props.virtualization.description;
    }

    if (
      props.routeState.virtualization &&
      props.routeState.virtualization.description
    ) {
      return props.routeState.virtualization.description;
    }

    return '';
  };

  return (
    <>
      <PageSection variant={'light'} noPadding={true}>
        <VirtualizationBreadcrumb
          actions={
            <VirtualizationActionContainer
              deleteActionProps={props.deleteActionCustomProps}
              exportActionProps={props.exportActionCustomProps}
              includeActions={props.actions}
              includeItems={props.items}
              postDeleteHref={resolvers.data.root()}
              publishActionProps={props.publishActionCustomProps}
              saveActionProps={props.saveActionCustomProps}
              startActionProps={props.startActionCustomProps}
              stopActionProps={props.stopActionCustomProps}
              virtualization={props.virtualization}
            />
          }
          dataPageHref={resolvers.data.root()}
          homePageHref={resolvers.dashboard.root()}
          i18nDataPageTitle={t('shared:Data')}
          i18nHomePageTitle={t('shared:Home')}
          i18nVirtualizationBreadcrumb={t('virtualizationNameBreadcrumb', {
            name: props.routeParams.virtualizationId,
          })}
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
          i18nPublishLogUrlText={t('shared:viewLogs')}
          i18nODataUrlText={t('viewOData')}
          modified={currPublishedState.modified}
          odataUrl={getOdataUrl(
            props.virtualization || props.routeState.virtualization
          )}
          publishedState={currPublishedState.state}
          publishedVersion={currPublishedState.version}
          publishingCurrentStep={currPublishedState.stepNumber}
          publishingLogUrl={currPublishedState.logUrl}
          publishingTotalSteps={currPublishedState.stepTotal}
          publishingStepText={currPublishedState.stepText}
          virtualizationDescription={getDescription()}
          virtualizationName={props.routeParams.virtualizationId}
          isWorking={!props.virtualization || !currPublishedState}
          onChangeDescription={doSetDescription}
        />
      </PageSection>
      <PageSection variant={'light'}>
        <VirtualizationNavBar virtualization={props.routeState.virtualization} />
      </PageSection>
      <PageSection variant={'light'} noPadding={true}>
        {props.children}
      </PageSection>
    </>
  );
};
