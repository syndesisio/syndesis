import { action } from '@storybook/addon-actions';
import { text, withKnobs } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import { Button, ListView } from 'patternfly-react';
import * as React from 'react';
import {
  CiCdListSkeleton,
  ITagIntegrationEntry,
  TagIntegrationDialog,
  TagIntegrationDialogEmptyState,
  TagIntegrationListItem,
} from '../../../src';

const stories = storiesOf('Integration/CiCd/TagIntegrationDialog', module);
stories.addDecorator(withKnobs);

const i18nTagIntegrationDialogMessage = text(
  'Dialog Message',
  'Tag this integration for release in one or more of the environments.'
);

stories
  .add('with children', () => (
    <TagIntegrationDialogStory
      loading={false}
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
    />
  ))
  .add('empty state', () => (
    <TagIntegrationDialogStory loading={false} items={[]} />
  ))
  .add('loading', () => (
    <TagIntegrationDialogStory loading={true} items={[]} />
  ));

interface ITagIntegrationDialogStoryProps {
  items: ITagIntegrationEntry[];
  loading: boolean;
}

interface ITagIntegrationDialogStoryState {
  showDialog: boolean;
}

class TagIntegrationDialogStory extends React.Component<
  ITagIntegrationDialogStoryProps,
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
            initialItems={this.props.items}
            i18nTitle={text(
              'Title',
              'Mark Integration for Continuous Integration/Continuous Deployment'
            )}
            i18nCancelButtonText={text('Cancel Button', 'Cancel')}
            i18nSaveButtonText={text('Save Button', 'Save')}
          >
            {({ handleChange, items }) => (
              <>
                <p>{i18nTagIntegrationDialogMessage}</p>
                {this.props.loading && (
                  <ListView>
                    <CiCdListSkeleton />
                  </ListView>
                )}
                {!this.props.loading && (
                  <>
                    {items.length > 0 && (
                      <ListView>
                        {items.map((item, index) => {
                          return (
                            <TagIntegrationListItem
                              key={index}
                              name={item.name}
                              selected={item.selected}
                              onChange={handleChange}
                            />
                          );
                        })}
                      </ListView>
                    )}
                    {items.length === 0 && (
                      <TagIntegrationDialogEmptyState
                        href={text('href', '#example')}
                        i18nTitle={text(
                          'Empty State Title',
                          'No Environments Available'
                        )}
                        i18nInfo={text('Empty State Info', '')}
                        i18nGoToManageCiCdButtonText={text(
                          'Go To Manage CI/CD',
                          'Go To Manage CI/CD'
                        )}
                      />
                    )}
                  </>
                )}
              </>
            )}
          </TagIntegrationDialog>
        )}
        <Button className="btn btn-primary" onClick={this.openDialog}>
          Open Dialog
        </Button>
      </>
    );
  }
}
