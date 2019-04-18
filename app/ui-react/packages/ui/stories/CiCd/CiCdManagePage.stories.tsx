import { action } from '@storybook/addon-actions';
import { text, withKnobs } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import { CiCdManagePage, ICiCdListPageItem } from '../../src';

const stories = storiesOf('CiCd/CiCdManagePage', module);
stories.addDecorator(withKnobs);

stories
  .add('with children', () => (
    <CiCdManagePageStory
      i18nConfirmRemoveMessageText={text(
        'Confirm Remove Message',
        'Are you sure you want to remove the ### tag'
      )}
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
      items={[]}
    />
  ));

interface ICiCdManagePageStoryProps {
  items: ICiCdListPageItem[];
  i18nConfirmRemoveMessageText: string;
}

class CiCdManagePageStory extends React.Component<ICiCdManagePageStoryProps> {
  constructor(props: ICiCdManagePageStoryProps) {
    super(props);
    this.createConfirmRemoveString = this.createConfirmRemoveString.bind(this);
  }
  public createConfirmRemoveString(name: string) {
    return this.props.i18nConfirmRemoveMessageText.replace('###', name);
  }
  public render() {
    return (
      <CiCdManagePage
        activeFilters={[]}
        currentFilterType={{
          filterType: 'text',
          id: 'name',
          placeholder: text('placeholder', 'Filter by name'),
          title: text('title', 'Name'),
        }}
        currentSortType={'sort'}
        currentValue={''}
        filterTypes={[]}
        isSortAscending={true}
        resultsCount={2}
        sortTypes={[]}
        onUpdateCurrentValue={action('onUpdateCurrentValue')}
        onValueKeyPress={action('onValueKeyPress')}
        onFilterAdded={action('onFilterAdded')}
        onSelectFilterType={action('onSelectFilterType')}
        onFilterValueSelected={action('onFilterValueSelected')}
        onRemoveFilter={action('onRemoveFilter')}
        onClearFilters={action('onClearFilters')}
        onToggleCurrentSortDirection={action('onToggleCurrentSortDirection')}
        onUpdateCurrentSortType={action('onUpdateCurrentSortType')}
        onAddNew={action('onAddNew')}
        onEditItem={action('onEditItem')}
        onAddItem={action('onAddItem')}
        onRemoveItem={action('onRemoveItem')}
        i18nRemoveButtonText={text('Remove Button Text', 'Remove')}
        i18nResultsCount={text('i18nResultsCount', '2 Results')}
        i18nAddNewButtonText={text('Add New Text', 'Add New')}
        i18nPageTitle={text('Page Title', 'Manage CI/CD')}
        i18nCancelButtonText={text('Dialog Cancel Text', 'Cancel')}
        i18nSaveButtonText={text('Dialog Save Text', 'Save')}
        i18nEditButtonText={text('Edit Button', 'Edit')}
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
        i18nEmptyStateTitle={text(
          'Empty State Title',
          'No Environments Available'
        )}
        listItems={this.props.items}
      />
    );
  }
}
