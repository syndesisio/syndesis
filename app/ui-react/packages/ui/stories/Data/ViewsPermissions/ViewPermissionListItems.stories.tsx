import { action } from '@storybook/addon-actions';
import { storiesOf } from '@storybook/react';
import * as React from 'react';

import { PageSection, ViewPermissionListItems } from '../../../src';

const stories = storiesOf('Data/ViewsPermissions/ViewPermissionListItems', module);

const onSelectedViewChangeActionText = 'onSelectedViewChange action';
const tablePrivileges = [
  {
    grantPrivileges: ['SELECT', 'INSERT', 'UPDATE', 'DELETE'],
    roleName: 'myRole1',
    viewDefinitionIds: ['1'],
  },
  {
    grantPrivileges: ['SELECT', 'INSERT', 'UPDATE'],
    roleName: 'myRole2',
    viewDefinitionIds: ['1'],
  },
  {
    grantPrivileges: ['SELECT', 'UPDATE'],
    roleName: 'myRole3',
    viewDefinitionIds: ['1'],
  },
  {
    grantPrivileges: ['SELECT'],
    roleName: 'myRole4',
    viewDefinitionIds: ['1'],
  },
  {
    grantPrivileges: ['SELECT', 'INSERT', 'UPDATE', 'DELETE'],
    roleName: 'myRole5',
    viewDefinitionIds: ['1'],
  },
  {
    grantPrivileges: ['SELECT', 'INSERT', 'UPDATE'],
    roleName: 'myRole6',
    viewDefinitionIds: ['1'],
  }
];
const map = new Map<string,string>([['1', 'View_1'],['2', 'view2'],['3', 'view3']]);

const sampleViewPermissionListItemsNotes =
  '- Verify one view item is shown\n' +
  '- Verify the view has 6 role items - 4 are initially displayed \n' +
  '- Verify click "2 more.." link displays all 6 roles"\n' +
  '- Verify click "Show less" link will change from 6 roles displayed back to 4 roles displayed';

const sampleViewPermissionListItemsNotes2 =
  '- Verify one view item is shown\n' +
  '- Verify the view has no Permission \n';

stories.add(
  'View with applied Permission',
  () => (
    <PageSection>
      <ViewPermissionListItems
        key={0}
        // i18nSelect={t('shared:Select')}
        // i18nInsert={t('shared:Insert')}
        // i18nUpdate={t('shared:Update')}
        // i18nDelete={t('shared:Delete')}
        // i18nAllAccess={t('allAccess')}
        // i18nRole={t('permissionRole')}
        // i18nAddNewRole={t('addNewRole')}
        itemSelected={map}
        viewId={'1'}
        viewName={'View_1'}
        i18nPermissionNotSet={'permission not set'}
        i18nShowLess={'Show less'}
        viewRolePermissionList={tablePrivileges}
        // status={dvStatus.attributes}
        onSelectedViewChange={action(onSelectedViewChangeActionText)}
        // dvRoles={dvRoles}
      />
    </PageSection>
  ),
  { notes: sampleViewPermissionListItemsNotes }
)
.add(
  'View with no Permission',
  () => (
    <PageSection>
      <ViewPermissionListItems
        key={0}
        // i18nSelect={t('shared:Select')}
        // i18nInsert={t('shared:Insert')}
        // i18nUpdate={t('shared:Update')}
        // i18nDelete={t('shared:Delete')}
        // i18nAllAccess={t('allAccess')}
        // i18nRole={t('permissionRole')}
        // i18nAddNewRole={t('addNewRole')}
        itemSelected={map}
        viewId={'1'}
        viewName={'View_1'}
        i18nPermissionNotSet={'permission not set'}
        i18nShowLess={'Show less'}
        viewRolePermissionList={[]}
        // status={dvStatus.attributes}
        onSelectedViewChange={action(onSelectedViewChangeActionText)}
        // dvRoles={dvRoles}
      />
    </PageSection>
  ),
  { notes: sampleViewPermissionListItemsNotes2 }
);