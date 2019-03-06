import { withNotes } from '@storybook/addon-notes';
import { storiesOf } from '@storybook/react';
import { Button } from 'patternfly-react';
import * as React from 'react';
import { DeleteConfirmationDialog } from '../../src';

const cancelText = 'Cancel';
const deleteMessage = 'Are you sure you want to delete this object?';
const deleteText = 'Delete';
const detailsMessage =
  'This is a message that provides more details about the delete.';
const showDetailsMessageDialogButtonText = 'Show dialog with details message';
const showNoDetailsMessageDialogButtonText =
  'Show dialog without details message';
const title = 'Confirm Delete?';

const stories = storiesOf('Shared/DeleteConfirmationDialog', module);

const detailsMessageStoryNotes =
  '- Verify the dialog displays when the "' +
  showDetailsMessageDialogButtonText +
  '" button is clicked\n' +
  '- Verify the dialog title is "' +
  title +
  '"\n' +
  '- Verify the dialog message is "' +
  deleteMessage +
  '"\n' +
  '- Verify the details message is "' +
  detailsMessage +
  '"\n' +
  '- Verify the cancel button text is "' +
  cancelText +
  '"\n' +
  '- Verify the delete button text is "' +
  deleteText +
  '"\n' +
  '- Verify clicking the delete button closes the dialog\n' +
  '- Verify clicking the cancel button closes the dialog\n' +
  '- Verify clicking the "X" button in the title closes the dialog\n';

const noDetailsMessageStoryNotes =
  '- Verify the dialog displays when the "' +
  showNoDetailsMessageDialogButtonText +
  '" button is clicked\n' +
  '- Verify the dialog title is "' +
  title +
  '"\n' +
  '- Verify the dialog message is "' +
  deleteMessage +
  '"\n' +
  '- Verify the cancel button text is "' +
  cancelText +
  '"\n' +
  '- Verify the delete button text is "' +
  deleteText +
  '"\n' +
  '- Verify clicking the delete button closes the dialog\n' +
  '- Verify clicking the cancel button closes the dialog\n' +
  '- Verify clicking the "X" button in the title closes the dialog\n';

stories
  .add(
    'no details',
    withNotes(noDetailsMessageStoryNotes)(() => <ConfirmationDialog />)
  )
  .add(
    'with details',
    withNotes(detailsMessageStoryNotes)(() => (
      <ConfirmationDialog includeDetailsMessage={true} />
    ))
  );

interface IConfirmationDialogProps {
  includeDetailsMessage?: boolean;
}

interface IConfirmationDialogState {
  show: boolean;
}

class ConfirmationDialog extends React.Component<
  IConfirmationDialogProps,
  IConfirmationDialogState
> {
  public constructor(props: IConfirmationDialogProps) {
    super(props);
    this.state = {
      show: false,
    };
    this.handleCancel = this.handleCancel.bind(this);
    this.handleDelete = this.handleDelete.bind(this);
    this.showModal = this.showModal.bind(this);
  }

  handleCancel = () => {
    this.setState(() => ({ show: false }));
  };

  handleDelete = () => {
    this.setState(() => ({ show: false }));
  };

  showModal = () => {
    this.setState(() => ({ show: true }));
  };

  public render() {
    return (
      <>
        <Button onClick={this.showModal} bsStyle="primary">
          {this.props.includeDetailsMessage
            ? showDetailsMessageDialogButtonText
            : showNoDetailsMessageDialogButtonText}
        </Button>
        <DeleteConfirmationDialog
          i18nCancelButtonText={cancelText}
          i18nDeleteButtonText={deleteText}
          i18nDeleteMessage={deleteMessage}
          i18nDetailsMessage={
            this.props.includeDetailsMessage ? detailsMessage : undefined
          }
          i18nTitle={title}
          onCancel={this.handleCancel}
          onDelete={this.handleDelete}
          showDialog={this.state.show}
        />
      </>
    );
  }
}
