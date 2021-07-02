import {
  Button,
  Modal,
  Split,
  SplitItem,
  Stack,
  StackItem,
  Text,
  Title,
} from '@patternfly/react-core';
import {
  ErrorCircleOIcon,
  InfoIcon,
  OkIcon,
  WarningTriangleIcon,
} from '@patternfly/react-icons';
import {
  global_danger_color_100,
  global_info_color_100,
  global_success_color_100,
  global_warning_color_100,
} from '@patternfly/react-tokens';
import * as React from 'react';

/**
 * Icon type enum that maps to patternfly icon types
 */
export enum ConfirmationIconType {
  DANGER = 'error-circle-o',
  WARNING = 'warning-triangle-o',
  INFO = 'info',
  OK = 'ok',
  NONE = 'NONE',
}

/**
 * Button style enum that maps to patternfly button classes
 */
export enum ConfirmationButtonStyle {
  NORMAL = 'primary',
  SUCCESS = 'success',
  DANGER = 'danger',
  WARNING = 'warning',
  INFO = 'info',
  LINK = 'link',
}

/**
 * A dialog that can be used to obtain user confirmation when deleting an object.
 */
export interface IConfirmationDialogProps {
  /**
   * The style of button to use for the primary action
   */
  buttonStyle: ConfirmationButtonStyle;
  /**
   * The localized cancel button text.
   */
  i18nCancelButtonText: string;

  /**
   * The localized confirmation button text.
   */
  i18nConfirmButtonText: string;

  /**
   * The localized confirmation message.
   */
  i18nConfirmationMessage: string;

  /**
   * An optional localized message providing more details.
   */
  i18nDetailsMessage?: string;

  /**
   * The localized dialog title.
   */
  i18nTitle: string;

  /**
   * The icon type to use, or unset for no icon
   */
  icon: ConfirmationIconType;

  /**
   * A callback for when the cancel button is clicked. Caller should hide dialog.
   */
  onCancel: () => void;

  /**
   * A callback for when the confirmation button is clicked. Caller should hide dialog.
   */
  onConfirm: () => void;

  /**
   * Indicates if the dialog should be visible.
   */
  showDialog: boolean;
}

/**
 * A modal dialog to display when an object is being deleted.
 */
export const ConfirmationDialog: React.FunctionComponent<IConfirmationDialogProps> =
  ({
    buttonStyle,
    i18nCancelButtonText,
    i18nConfirmButtonText,
    i18nConfirmationMessage,
    i18nDetailsMessage,
    i18nTitle,
    icon,
    onCancel,
    onConfirm,
    showDialog,
  }) => {
    let iconFragment: React.ReactNode | null = null;
    switch (icon) {
      case ConfirmationIconType.DANGER:
        iconFragment = (
          <ErrorCircleOIcon size={'lg'} color={global_danger_color_100.value} />
        );
        break;
      case ConfirmationIconType.WARNING:
        iconFragment = (
          <WarningTriangleIcon
            size={'lg'}
            color={global_warning_color_100.value}
          />
        );
        break;
      case ConfirmationIconType.INFO:
        iconFragment = (
          <InfoIcon size={'lg'} color={global_info_color_100.value} />
        );
        break;
      case ConfirmationIconType.OK:
        iconFragment = (
          <OkIcon size={'lg'} color={global_success_color_100.value} />
        );
        break;
      default:
      // No icon
    }
    let buttonStyleMapped:
      | 'primary'
      | 'secondary'
      | 'tertiary'
      | 'danger'
      | 'link'
      | 'plain'
      | 'control' = 'primary';
    switch (buttonStyle) {
      case 'danger':
        buttonStyleMapped = 'danger';
        break;
      case 'info':
        buttonStyleMapped = 'secondary';
        break;
      case 'link':
        buttonStyleMapped = 'link';
        break;
      default:
    }
    return (
      <Modal
        title={i18nTitle}
        isOpen={showDialog}
        onClose={onCancel}
        actions={[
          <Button key="confirm" variant={buttonStyleMapped} onClick={onConfirm}>
            {i18nConfirmButtonText}
          </Button>,
          <Button key="cancel" variant="link" onClick={onCancel}>
            {i18nCancelButtonText}
          </Button>,
        ]}
        width={'50%'}
      >
        <Stack hasGutter={true}>
          <StackItem>
            <Split hasGutter={true}>
              {iconFragment && <SplitItem>{iconFragment}</SplitItem>}
              <SplitItem isFilled={true}>
                <Title size={'lg'} headingLevel={'h4'}>
                  {i18nConfirmationMessage}
                </Title>
              </SplitItem>
            </Split>
          </StackItem>
          {i18nDetailsMessage && (
            <StackItem>
              <Text>{i18nDetailsMessage}</Text>
            </StackItem>
          )}
        </Stack>
      </Modal>
    );
  };
