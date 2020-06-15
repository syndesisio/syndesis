import { action } from '@storybook/addon-actions';
import { storiesOf } from '@storybook/react';
import * as React from 'react';

import { PageSection } from '@patternfly/react-core';
import { ITablePrivilege, RolePermissionList } from '../../../src';

const stories = storiesOf('Data/ViewsPermissions/RolePermissionList', module);

const roleText = 'Role';
const selectText = 'Select';
const insertText = 'Insert';
const updateText = 'Update';
const deleteText = 'Delete';
const allAccessText = 'All Access';
const addNewRoleText = 'Add new role';
const selectRoleText = 'Select a role';
const removeRoleRowText = 'Remove role row';
const roleExistsText =
  'A role with this name already exists - using the existing role.';
const roles = ['Developer', 'User', 'Admin'];

const updateRolePermissionModelActionText =
  'Update role permission model action';
const deleteRoleFromPermissionModelActionText =
  'Delete role from permission model action';
const rolePermissionList: ITablePrivilege[] = [];

const sampleRolePermissionListNotes =
  '- Verify list header is shown\n' +
  '- Verify list header titles are aligned with row item content\n' +
  '- Verify a single role row is shown\n' +
  '- Verify the role dropdown selections are: (' +
  roles +
  ')\n' +
  '- Verify icon to add role is shown at bottom with text "' +
  addNewRoleText +
  '"\n' +
  '- Verify clicking on the icon to add role adds another list row\n' +
  '- Verify clicking on the remove row icon removes the row';

stories.add(
  'sample permission list',
  () => (
    <PageSection>
      <RolePermissionList
        i18nRole={roleText}
        i18nSelect={selectText}
        i18nInsert={insertText}
        i18nUpdate={updateText}
        i18nDelete={deleteText}
        i18nAllAccess={allAccessText}
        i18nAddNewRole={addNewRoleText}
        i18nSelectRole={selectRoleText}
        i18nRemoveRoleRow={removeRoleRowText}
        i18nRoleExists={roleExistsText}
        viewRolePermissionList={rolePermissionList}
        updateRolePermissionModel={action(updateRolePermissionModelActionText)}
        selectedRoles={new Map()}
        roles={roles}
        deleteRoleFromPermissionModel={action(
          deleteRoleFromPermissionModelActionText
        )}
        clearAction={false}
      />
    </PageSection>
  ),
  { notes: sampleRolePermissionListNotes }
);
