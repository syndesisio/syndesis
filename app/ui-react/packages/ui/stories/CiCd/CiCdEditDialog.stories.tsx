import { text, withKnobs } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import { Button } from 'patternfly-react';
import * as React from 'react';
import { CiCdEditDialog } from '../../src';

const stories = storiesOf('CiCd/EditDialog', module);
stories.addDecorator(withKnobs);

stories
  .add('edit tag', () => (
    <CiCdDialogStory
      i18nTitle={text('Dialog Title', 'Edit Tag')}
      i18nDescription={text(
        'Dialog Description',
        'The following changes will be applied to all integrations.'
      )}
      i18nSaveButtonText={text('Save Button Text', 'Save')}
      i18nCancelButtonText={text('Cancel Button Text', 'Cancel')}
      i18nInputLabel={text('Input Label', 'Tag Name')}
      tagName={text('Tag Name', 'Staging')}
    />
  ))
  .add('add new tag', () => (
    <CiCdDialogStory
      i18nTitle={text('Dialog Title', 'Add New Tag')}
      i18nDescription={text(
        'Dialog Description',
        'The following changes will be applied to all integrations.'
      )}
      i18nSaveButtonText={text('Save Button Text', 'Save')}
      i18nCancelButtonText={text('Cancel Button Text', 'Cancel')}
      i18nInputLabel={text('Input Label', 'Tag Name')}
      tagName={text('Tag Name', '')}
    />
  ));

interface ICiCdDialogStoryProps {
  i18nTitle: string;
  i18nDescription: string;
  tagName: string;
  i18nInputLabel: string;
  i18nSaveButtonText: string;
  i18nCancelButtonText: string;
}

interface ICiCdDialogStoryState {
  showDialog: boolean;
}

class CiCdDialogStory extends React.Component<
  ICiCdDialogStoryProps,
  ICiCdDialogStoryState
> {
  constructor(props) {
    super(props);
    this.state = {
      showDialog: true,
    };
    this.openDialog = this.openDialog.bind(this);
    this.closeDialog = this.closeDialog.bind(this);
  }
  public openDialog() {
    this.setState({ showDialog: true });
  }
  public closeDialog() {
    this.setState({ showDialog: false });
  }
  public render() {
    return (
      <>
        {this.state.showDialog && (
          <CiCdEditDialog
            i18nTitle={this.props.i18nTitle}
            i18nDescription={this.props.i18nDescription}
            i18nCancelButtonText={this.props.i18nCancelButtonText}
            i18nInputLabel={this.props.i18nInputLabel}
            i18nSaveButtonText={this.props.i18nSaveButtonText}
            tagName={this.props.tagName}
            onSave={this.closeDialog}
            onHide={this.closeDialog}
          />
        )}
        <Button className="btn btn-primary" onClick={this.openDialog}>
          Open Dialog
        </Button>
      </>
    );
  }
}
