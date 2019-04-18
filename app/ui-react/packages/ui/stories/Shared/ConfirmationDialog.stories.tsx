import { storiesOf } from '@storybook/react';
import { Button } from 'patternfly-react';
import * as React from 'react';
import {
  ConfirmationButtonStyle,
  ConfirmationDialog,
  ConfirmationIconType,
} from '../../src';

const cancelText = 'Cancel';
const deleteMessage = 'Are you sure you want to delete this object?';
const deleteText = 'Delete';
const detailsMessage =
  'This is a message that provides more details about the delete.';
const showDetailsMessageDialogButtonText = 'Show dialog with details message';
const showNoDetailsMessageDialogButtonText =
  'Show dialog without details message';
const title = 'Confirm Delete?';

const stories = storiesOf('Shared/ConfirmationDialog', module);

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

const noDetailsMessageDangerStoryNotes =
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

const noDetailsMessageWarningStoryNotes =
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
    'no details - danger',
    () => (
      <ConfirmationDialogStory
        buttonStyle={ConfirmationButtonStyle.DANGER}
        icon={ConfirmationIconType.DANGER}
      />
    ),
    {
      notes: noDetailsMessageDangerStoryNotes,
    }
  )
  .add(
    'no details - warning',
    () => (
      <ConfirmationDialogStory
        buttonStyle={ConfirmationButtonStyle.WARNING}
        icon={ConfirmationIconType.WARNING}
      />
    ),
    {
      notes: noDetailsMessageWarningStoryNotes,
    }
  )
  .add(
    'with details',
    () => (
      <ConfirmationDialogStory
        includeDetailsMessage={true}
        buttonStyle={ConfirmationButtonStyle.DANGER}
        icon={ConfirmationIconType.DANGER}
      />
    ),
    { notes: detailsMessageStoryNotes }
  )
  .add('no icon', () => (
    <ConfirmationDialogStory
      includeDetailsMessage={false}
      buttonStyle={ConfirmationButtonStyle.NORMAL}
      icon={ConfirmationIconType.NONE}
    />
  ));

interface IConfirmationDialogStoryProps {
  includeDetailsMessage?: boolean;
  buttonStyle: ConfirmationButtonStyle;
  icon: ConfirmationIconType;
}

interface IConfirmationDialogStoryState {
  show: boolean;
}

class ConfirmationDialogStory extends React.Component<
  IConfirmationDialogStoryProps,
  IConfirmationDialogStoryState
> {
  public constructor(props: IConfirmationDialogStoryProps) {
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
        <Button
          onClick={this.showModal}
          bsStyle="primary"
          style={{ height: '10%', margin: '50px', width: '50%' }}
        >
          {this.props.includeDetailsMessage
            ? showDetailsMessageDialogButtonText
            : showNoDetailsMessageDialogButtonText}
        </Button>
        <ConfirmationDialog
          buttonStyle={this.props.buttonStyle}
          i18nCancelButtonText={cancelText}
          i18nConfirmButtonText={deleteText}
          i18nConfirmationMessage={deleteMessage}
          i18nDetailsMessage={
            this.props.includeDetailsMessage ? detailsMessage : undefined
          }
          i18nTitle={title}
          icon={this.props.icon}
          onCancel={this.handleCancel}
          onConfirm={this.handleDelete}
          showDialog={this.state.show}
        />
      </>
    );
  }
}
