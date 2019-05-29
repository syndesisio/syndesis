import ReactRouterPause from '@allpro/react-router-pause';
import * as H from '@syndesis/history';
import {
  ConfirmationButtonStyle,
  ConfirmationDialog,
  ConfirmationIconType,
} from '@syndesis/ui';
import * as React from 'react';
import { Translation } from 'react-i18next';

export interface IWithLeaveConfirmationChildrenProps {
  allowNavigation: () => any;
}

export interface IWithLeaveConfirmationBaseProps {
  i18nCancelButtonText?: string;
  i18nConfirmButtonText?: string;
  i18nConfirmationMessage?: string;
  i18nTitle?: string;
  shouldDisplayDialog?: (location: H.LocationDescriptor) => boolean;
}
export interface IWithLeaveConfirmationProps
  extends IWithLeaveConfirmationBaseProps {
  children: (props: IWithLeaveConfirmationChildrenProps) => any;
}

export interface IDialogProps {
  showDialog: boolean;
  onCancel: () => void;
  onConfirm: () => void;
}

export const WithLeaveConfirmation: React.FunctionComponent<
  IWithLeaveConfirmationProps
> = ({
  i18nCancelButtonText,
  i18nConfirmButtonText,
  i18nConfirmationMessage,
  i18nTitle,
  shouldDisplayDialog,
  children,
}) => {
  const [blockNavigation, setBlockNavigation] = React.useState(true);

  const initialDialogProps = {
    onCancel: () => false,
    onConfirm: () => false,
    showDialog: false,
  };

  const [dialogProps, setDialogProps] = React.useState<IDialogProps>(
    initialDialogProps
  );
  const closeDialog = () => setDialogProps(initialDialogProps);

  const handleNavigationAttempt = (
    navigation: any,
    location: any,
    action: any
  ) => {
    const showDialog = shouldDisplayDialog
      ? shouldDisplayDialog(location)
      : true;
    setDialogProps({
      onCancel: () => {
        closeDialog();
        navigation.cancel();
      },
      onConfirm: () => {
        closeDialog();
        navigation.resume();
      },
      showDialog,
    });
    // Return null to 'pause' and save the route so can 'resume'
    return showDialog ? null : true;
  };

  const allowNavigation = () => {
    setBlockNavigation(false);
  };
  return (
    <Translation ns={['integrations', 'shared']}>
      {t => (
        <>
          <ReactRouterPause
            handler={handleNavigationAttempt}
            when={blockNavigation}
            config={{ allowBookmarks: false }}
          />

          <ConfirmationDialog
            buttonStyle={ConfirmationButtonStyle.NORMAL}
            icon={ConfirmationIconType.WARNING}
            i18nCancelButtonText={i18nCancelButtonText || t('shared:Cancel')}
            i18nConfirmButtonText={i18nConfirmButtonText || t('shared:Confirm')}
            i18nConfirmationMessage={
              i18nConfirmationMessage || t('shared:confirmLeavingPageMessage')
            }
            i18nTitle={i18nTitle || t('shared:confirmLeavingPageTitle')}
            {...dialogProps}
          />

          {children({ allowNavigation })}
        </>
      )}
    </Translation>
  );
};
