import { Icon, MessageDialog } from 'patternfly-react';
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
export class ConfirmationDialog extends React.Component<
  IConfirmationDialogProps
> {
  public render() {
    return (
      <MessageDialog
        accessibleName="deleteConfirmationDialog"
        accessibleDescription="deleteConfirmationDialogContent"
        icon={
          this.props.icon !== ConfirmationIconType.NONE && (
            <Icon type="pf" name={this.props.icon} />
          )
        }
        onHide={this.props.onCancel}
        primaryAction={this.props.onConfirm}
        primaryActionButtonContent={this.props.i18nConfirmButtonText}
        primaryActionButtonBsStyle={this.props.buttonStyle}
        primaryContent={
          <p className="lead">{this.props.i18nConfirmationMessage}</p>
        }
        secondaryAction={this.props.onCancel}
        secondaryActionButtonContent={this.props.i18nCancelButtonText}
        secondaryContent={
          this.props.i18nDetailsMessage ? (
            <p>{this.props.i18nDetailsMessage}</p>
          ) : (
            undefined
          )
        }
        show={this.props.showDialog}
        title={this.props.i18nTitle}
      />
    );
  }
}
