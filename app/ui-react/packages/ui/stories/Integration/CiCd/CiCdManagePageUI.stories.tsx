import { action } from '@storybook/addon-actions';
import { text, withKnobs } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import {
  CiCdList,
  CiCdListEmptyState,
  CiCdListItem,
  CiCdListSkeleton,
  CiCdManagePageUI,
  Container,
  ICiCdListPageItem,
  TagNameValidationError,
} from '../../../src';

const stories = storiesOf('Integration/CiCd/CiCdManagePageUI', module);
stories.addDecorator(withKnobs);

const addNewButtonText = text('Add New Text', 'Add New');

stories
  .add('with children', () => (
    <CiCdManagePageStory
      i18nConfirmRemoveMessageText={text(
        'Confirm Remove Message',
        'Are you sure you want to remove the ### tag'
      )}
      loading={false}
      items={[
        {
          i18nUsesText: 'Used by 3 integrations',
          name: 'Development',
        },
        {
          i18nUsesText: 'Used by 2 integrations',
          name: 'Staging',
        },
        {
          i18nUsesText: 'Used by 0 integrations',
          name: 'UAT',
        },
        {
          i18nUsesText: 'Used by 0 integrations',
          name: 'Production',
        },
      ]}
    />
  ))
  .add('empty state', () => (
    <CiCdManagePageStory
      i18nConfirmRemoveMessageText={text(
        'Confirm Remove Message',
        'Are you sure you want to remove the ### tag'
      )}
      loading={false}
      items={[]}
    />
  ))
  .add('loading', () => (
    <CiCdManagePageStory
      i18nConfirmRemoveMessageText={text(
        'Confirm Remove Message',
        'Are you sure you want to remove the ### tag'
      )}
      loading={true}
      items={[]}
    />
  ));

interface ICiCdManagePageStoryProps {
  items: ICiCdListPageItem[];
  loading: boolean;
  i18nConfirmRemoveMessageText: string;
}

interface ICiCdManagePageStoryState {
  nameValidationError: TagNameValidationError;
}

class CiCdManagePageStory extends React.Component<
  ICiCdManagePageStoryProps,
  ICiCdManagePageStoryState
> {
  constructor(props: ICiCdManagePageStoryProps) {
    super(props);
    this.state = {
      nameValidationError: TagNameValidationError.NoErrors,
    };
    this.createConfirmRemoveString = this.createConfirmRemoveString.bind(this);
    this.handleNameValidation = this.handleNameValidation.bind(this);
  }
  public handleNameValidation(name: string) {
    if (!name || name === '') {
      this.setState({ nameValidationError: TagNameValidationError.NoName });
    } else if (name === 'UsedTag') {
      this.setState({ nameValidationError: TagNameValidationError.NameInUse });
    } else {
      this.setState({ nameValidationError: TagNameValidationError.NoErrors });
    }
  }
  public createConfirmRemoveString(name: string) {
    return this.props.i18nConfirmRemoveMessageText.replace('###', name);
  }
  public render() {
    return (
      <Container>
        <CiCdManagePageUI
          activeFilters={[]}
          currentFilterType={{
            filterType: 'text',
            id: 'name',
            placeholder: text('placeholder', 'Filter by name'),
            title: text('title', 'Name'),
          }}
          currentSortType={{
            id: 'sort',
            isNumeric: false,
            title: 'Sort',
          }}
          currentValue={''}
          filterTypes={[]}
          isSortAscending={true}
          resultsCount={this.props.items.length}
          sortTypes={[]}
          nameValidationError={this.state.nameValidationError}
          onUpdateCurrentValue={action('onUpdateCurrentValue')}
          onValueKeyPress={action('onValueKeyPress')}
          onFilterAdded={action('onFilterAdded')}
          onSelectFilterType={action('onSelectFilterType')}
          onFilterValueSelected={action('onFilterValueSelected')}
          onRemoveFilter={action('onRemoveFilter')}
          onClearFilters={action('onClearFilters')}
          onToggleCurrentSortDirection={action('onToggleCurrentSortDirection')}
          onUpdateCurrentSortType={action('onUpdateCurrentSortType')}
          onEditItem={action('onEditItem')}
          onAddItem={action('onAddItem')}
          onRemoveItem={action('onRemoveItem')}
          onValidateItem={this.handleNameValidation}
          i18nResultsCount={text('i18nResultsCount', '2 Results')}
          i18nAddNewButtonText={addNewButtonText}
          i18nPageTitle={text('Page Title', 'Manage CI/CD')}
          i18nCancelButtonText={text('Dialog Cancel Text', 'Cancel')}
          i18nSaveButtonText={text('Dialog Save Text', 'Save')}
          i18nConfirmRemoveButtonText={text('Confirm Remove Button', 'Yes')}
          i18nConfirmCancelButtonText={text('Confirm Cancel Button', 'No')}
          i18nRemoveConfirmationMessage={this.createConfirmRemoveString}
          i18nRemoveConfirmationTitle={text(
            'Confirm Remove Message Title',
            'Confirm Remove?'
          )}
          i18nRemoveConfirmationDetailMessage={text(
            'Confirm Remove Message Detail',
            'This change will be applied to all integrations.  Removing a tag will unassociate it across integrations and then delete it.  No integrations will be deleted. '
          )}
          i18nAddTagDialogTitle={text('Add Dialog Title', 'Add Tag Name')}
          i18nAddTagDialogDescription={text(
            'Add Dialog Description',
            'The following changes will be applied to all integrations.'
          )}
          i18nEditTagDialogTitle={text('Edit Dialog Title', 'Edit Tag')}
          i18nEditTagDialogDescription={text(
            'Edit Dialog Description',
            'The following changes will be applied to all integrations.'
          )}
          i18nTagInputLabel={text('Dialog Tag Label', 'Tag Name')}
          i18nPageDescription={text(
            'Page Description',
            'This description has not yet been actually defined, please send help.'
          )}
          i18nNoNameError={text('No Name Error', 'Please enter a tag name.')}
          i18nNameInUseError={text(
            'Name in Use Error',
            'That tag name is already in use.'
          )}
        >
          {({ openAddDialog, openEditDialog, openRemoveDialog }) => (
            <>
              {this.props.loading && (
                <CiCdList children={<CiCdListSkeleton />} />
              )}
              {!this.props.loading && (
                <>
                  {this.props.items.length !== 0 && (
                    <CiCdList
                      children={this.props.items.map((listItem, index) => (
                        <CiCdListItem
                          key={index}
                          onEditClicked={openEditDialog}
                          onRemoveClicked={openRemoveDialog}
                          i18nEditButtonText={text('Edit Button', 'Edit')}
                          i18nRemoveButtonText={text(
                            'Remove Button Text',
                            'Remove'
                          )}
                          name={listItem.name}
                          i18nUsesText={listItem.i18nUsesText}
                        />
                      ))}
                    />
                  )}
                  {this.props.items.length === 0 && (
                    <CiCdListEmptyState
                      onAddNew={openAddDialog}
                      i18nTitle={text(
                        'Empty State Title',
                        'No Environments Available'
                      )}
                      i18nAddNewButtonText={addNewButtonText}
                      i18nInfo={text('Empty State Info', '')}
                    />
                  )}
                </>
              )}
            </>
          )}
        </CiCdManagePageUI>
      </Container>
    );
  }
}
