import { action } from '@storybook/addon-actions';
import { text } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import * as React from 'react';

import { ViewListItem } from '../../../src';

const stories = storiesOf('Data/Views/ViewListItem', module);

const viewName = 'CustomersView';
const viewDescription = 'View description for CustomersView';
const deleteText = 'Delete';
const deleteActionText = 'Delete ' + viewName + ' View';
const editText = 'Edit';
const editTip = 'Edit ' + viewName + ' view';

const sampleViewNotes =
  '- Verify view icon is showing on the left\n' +
  '- Verify view name is "' +
  viewName +
  '"\n' +
  '- Verify view description is "' +
  viewDescription +
  '"\n' +
  '- Verify the edit button text is "' +
  editText +
  '"\n' +
  '- Verify the edit button tooltip is "' +
  editTip +
  '"\n' +
  '- Verify the kebab menu contains action "' +
  deleteText +
  '"\n' +
  '- Verify selecting Delete in the kebab menu prints "' +
  deleteActionText +
  '" in the ACTION LOGGER';

stories.add(
  'valid view item',
  () => (
    <ViewListItem
      viewId="viewListItem1"
      viewName={text('viewName', viewName)}
      viewDescription={text('viewDescription', viewDescription)}
      viewEditPageLink={''}
      viewIcon={text('icon', null)}
      i18nCancelText={'Cancel'}
      i18nDelete={text('deleteText', deleteText)}
      i18nDeleteModalMessage={'Do you really want to delete the view?'}
      i18nDeleteModalTitle={'Confirm Delete'}
      i18nEdit={text('editText', editText)}
      i18nEditTip={text('editTip', editTip)}
      i18nInvalid={'Invalid'}
      isValid={true}
      onDelete={action(deleteActionText)}
    />
  ),
  { notes: sampleViewNotes }
);

stories.add(
  'invalid view item',
  () => (
    <ViewListItem
      viewId="invalidViewListItem"
      viewName={text('viewName', viewName)}
      viewDescription={text('viewDescription', viewDescription)}
      viewEditPageLink={''}
      viewIcon={text('icon', null)}
      i18nCancelText={'Cancel'}
      i18nDelete={text('deleteText', deleteText)}
      i18nDeleteModalMessage={'Do you really want to delete the view?'}
      i18nDeleteModalTitle={'Confirm Delete'}
      i18nEdit={text('editText', editText)}
      i18nEditTip={text('editTip', editTip)}
      i18nInvalid={'Invalid'}
      isValid={false}
      onDelete={action(deleteActionText)}
    />
  ),
  { notes: sampleViewNotes }
);
