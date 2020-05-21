import { action } from '@storybook/addon-actions';
import { storiesOf } from '@storybook/react';
import * as React from 'react';

import { Grid, GridItem } from '@patternfly/react-core';
import { RolePermissionListItem } from '../../../src';

const stories = storiesOf(
  'Data/ViewsPermissions/RolePermissionListItem',
  module
);

const addRoleActionText = 'Add role action';
const updateRolePermissionModelActionText =
  'Update role permission model action';
const deleteRoleFromPermissionModelActionText =
  'Delete role from permission action';
const removeRolePermissionActionText = 'Remove role permission action';

const sampleRolePermissionItemNotes =
  '- Verify role drop down is shown on the left\n' +
  '- Verify five checkboxes are shown to the right of the dropdown.  The boxes represent (SELECT, INSERT, UPDATE, DELETE, ALL)\n' +
  '- Verify a remove role icon is shown at the far right\n' +
  '- Verify typing a new role name in the editable dropdown will show a "' +
  'create role" action in the dropdown\n' +
  '- Verify clicking the create role action prints "' +
  addRoleActionText +
  '" in the ACTION LOGGER\n' +
  '- Verify clicking any of the five checkboxes prints "' +
  updateRolePermissionModelActionText +
  '" in the ACTION LOGGER\n' +
  '-   the update should include the roleName with an array of the checked boxes (SELECT, INSERT, UPDATE, DELETE)\n' +
  '- Verify clicking on the remove row icon prints "' +
  removeRolePermissionActionText +
  '" in the ACTION LOGGER';

stories.add(
  'role permission item',
  () => (
    <Grid>
      <GridItem span={10}>
        <RolePermissionListItem
          index={'index'}
          availableRoles={['role1']}
          roles={['role1']}
          selectedRole={'role1'}
          selectedPermissions={['SELECT']}
          i18nSelectRole={'Select a role'}
          i18nRemoveRoleRow={'Remove role'}
          i18nRoleExists={'Role already exists'}
          updateRolePermissionModel={action(
            updateRolePermissionModelActionText
          )}
          deleteRoleFromPermissionModel={action(
            deleteRoleFromPermissionModelActionText
          )}
          removeRolePermission={action(removeRolePermissionActionText)}
        />
      </GridItem>
    </Grid>
  ),
  { notes: sampleRolePermissionItemNotes }
);
