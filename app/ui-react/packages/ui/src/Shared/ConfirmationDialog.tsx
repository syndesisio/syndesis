import { Icon, MessageDialog } from 'patternfly-react';
import * as React from 'react';

const literal = <L extends string>(l: L) => l;
export const ConfirmationDialogType = {
  DANGER: literal('DANGER'),
  INFO: literal('INFO'),
  WARNING: literal('WARNING'),
};
export type ConfirmationDialogType = (typeof ConfirmationDialogType)[keyof typeof ConfirmationDialogType];

/**
 * A dialog that can be used to obtain user confirmation.
 */
export interface IConfirmationDialogProps {
  /**
   * The localized cancel button text.
   */
  i18nCancelButtonText: string;

  /**
   * The localized accept button text.
   */
  i18nAcceptButtonText: string;

  /**
   * The localized accept confirmation message.
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
   * Confirmation type (DANGER, WARNING, INFO)
   */
  confirmationType: ConfirmationDialogType;

  /**
   * A callback for when the cancel button is clicked. Caller should hide dialog.
   */
  onCancel: () => void;

  /**
   * A callback for when the accept button is clicked. Caller should hide dialog.
   */
  onAccept: () => void;

  /**
   * Indicates if the dialog should be visible.
   */
  showDialog: boolean;
}

/**
 * A modal dialog to display for user confirmation.
 */
export class ConfirmationDialog extends React.Component<
  IConfirmationDialogProps
> {
  public render() {
    // Determine icon and button style based on dialog type
    let iconName = 'error-circle-o';
    let buttonStyle = 'danger';
    switch (this.props.confirmationType) {
      case ConfirmationDialogType.DANGER:
        iconName = 'error-circle-o';
        buttonStyle = 'danger';
        break;
      case ConfirmationDialogType.WARNING:
        iconName = 'warning-triangle-o';
        buttonStyle = 'primary';
        break;
      case ConfirmationDialogType.INFO:
        iconName = 'info';
        buttonStyle = 'primary';
    }

    return (
      <MessageDialog
        accessibleName="confirmationDialog"
        accessibleDescription="confirmationDialogContent"
        icon={<Icon type="pf" name={iconName} />}
        onHide={this.props.onCancel}
        primaryAction={this.props.onAccept}
        primaryActionButtonContent={this.props.i18nAcceptButtonText}
        primaryActionButtonBsStyle={buttonStyle}
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
