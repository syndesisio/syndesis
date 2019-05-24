import { Button } from 'patternfly-react';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { Dialog } from '../../src';

const stories = storiesOf('Shared/Dialog', module);

stories.add('example', () => <DialogStory />);

interface IDialogStoryState {
  showDialog: boolean;
}

class DialogStory extends React.Component<{}, IDialogStoryState> {
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
          <Dialog
            title={'This is my dialog'}
            body={
              <>
                <p>Hey there, it's a dialog</p>
                <p>And it's got some stuff in it</p>
              </>
            }
            footer={
              <Button className="btn btn-primary" onClick={this.closeDialog}>
                Great!
              </Button>
            }
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
