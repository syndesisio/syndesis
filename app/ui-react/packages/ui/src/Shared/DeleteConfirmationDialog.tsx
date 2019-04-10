import { Icon, MessageDialog } from 'patternfly-react';
import * as React from 'react';

/**
 * A dialog that can be used to obtain user confirmation when deleting an object.
 */
export interface IDeleteConfirmationDialogProps {
  /**
   * The localized cancel button text.
   */
  i18nCancelButtonText: string;

  /**
   * The localized delete button text.
   */
  i18nDeleteButtonText: string;

  /**
   * The localized delete confirmation message.
   */
  i18nDeleteMessage: string;

  /**
   * An optional localized message providing more details.
   */
  i18nDetailsMessage?: string;

  /**
   * The localized dialog title.
   */
  i18nTitle: string;

  /**
   * A callback for when the cancel button is clicked. Caller should hide dialog.
   */
  onCancel: () => void;

  /**
   * A callback for when the delete button is clicked. Caller should hide dialog.
   */
  onDelete: () => void;

  /**
   * Indicates if the dialog should be visible.
   */
  showDialog: boolean;
}

/**
 * A modal dialog to display when an object is being deleted.
 */
export class DeleteConfirmationDialog extends React.Component<
  IDeleteConfirmationDialogProps
> {
  public render() {
    return (
      <MessageDialog
        accessibleName="deleteConfirmationDialog"
        accessibleDescription="deleteConfirmationDialogContent"
        icon={<Icon type="pf" name="error-circle-o" />}
        onHide={this.props.onCancel}
        primaryAction={this.props.onDelete}
        primaryActionButtonContent={this.props.i18nDeleteButtonText}
        primaryActionButtonBsStyle="danger"
        primaryContent={<p className="lead">{this.props.i18nDeleteMessage}</p>}
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
