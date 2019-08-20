import { action } from '@storybook/addon-actions';
import { text } from '@storybook/addon-knobs';
import { withNotes } from '@storybook/addon-notes';
import { storiesOf } from '@storybook/react';
import * as React from 'react';

import { ViewListItem } from '../../../src';

const stories = storiesOf('Data/Views/ViewListItem', module);

const viewName = 'CustomersView';
const viewDescription = 'View description for CustomersView';
const deleteText = 'Delete';
const deleteTip = 'Delete ' + viewName + ' view';
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
  '- Verify the delete action tooltip is "' +
  deleteTip +
  '"\n' +
  '- Verify selecting Delete in the kebab menu prints "' +
  deleteActionText +
  '" in the ACTION LOGGER';

stories.add(
  'sample view item',
  withNotes(sampleViewNotes)(() => (
    <ViewListItem
      viewId="viewListItem1"
      viewName={text('viewName', viewName)}
      viewDescription={text('viewDescription', viewDescription)}
      viewEditPageLink={''}
      viewIcon={text('icon', null)}
      i18nCancelText={'Cancel'}
      i18nDelete={text('deleteText', deleteText)}
      i18nDeleteTip={text('deleteTip', deleteTip)}
      i18nDeleteModalMessage={'Do you really want to delete the view?'}
      i18nDeleteModalTitle={'Confirm Delete'}
      i18nEdit={text('editText', editText)}
      i18nEditTip={text('editTip', editTip)}
      onDelete={action(deleteActionText)}
    />
  ))
);
