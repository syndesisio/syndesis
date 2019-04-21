import { action } from '@storybook/addon-actions';
import { text, withKnobs } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import { Button } from 'patternfly-react';
import * as React from 'react';
import { TagIntegrationDialog } from '../../../src/Integration/CiCd';

const stories = storiesOf('Integration/CiCd/TagIntegrationDialog', module);
stories.addDecorator(withKnobs);

stories.add('Normal', () => <TagIntegrationDialogStory />);

interface ITagIntegrationDialogStoryState {
  showDialog: boolean;
}

class TagIntegrationDialogStory extends React.Component<
  {},
  ITagIntegrationDialogStoryState
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
          <TagIntegrationDialog
            onHide={this.closeDialog}
            onSave={action('onSave')}
            items={[
              {
                name: 'Development',
                selected: true,
              },
              {
                name: 'Staging',
                selected: true,
              },
              {
                name: 'Production',
                selected: false,
              },
            ]}
            i18nTitle={text(
              'Title',
              'Mark Integration for Continuous Integration/Continuous Deployment'
            )}
            i18nCancelButtonText={text('Cancel Button', 'Cancel')}
            i18nSaveButtonText={text('Save Button', 'Save')}
            i18nTagIntegrationDialogMessage={text(
              'Dialog Message',
              'Tag this integration for release in one or more of the environments.'
            )}
          />
        )}
        <Button className="btn btn-primary" onClick={this.openDialog}>
          Open Dialog
        </Button>
      </>
    );
  }
}
