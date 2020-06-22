import { action } from '@storybook/addon-actions';
import { storiesOf } from '@storybook/react';
import * as React from 'react';

import { PageSection, ViewPermissionListItems } from '../../../src';

const stories = storiesOf(
  'Data/ViewsPermissions/ViewPermissionListItems',
  module
);

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
  },
];
const map = new Map<string, string>([
  ['1', 'View_1'],
  ['2', 'view2'],
  ['3', 'view3'],
]);

const sampleViewPermissionListItemsNotes =
  '- Verify one view item is shown\n' +
  '- Verify the view has 6 role items - 4 are initially displayed \n' +
  '- Verify click "2 more.." link displays all 6 roles"\n' +
  '- Verify click "Show less" link will change from 6 roles displayed back to 4 roles displayed';

const sampleViewPermissionListItemsNotes2 =
  '- Verify one view item is shown\n' +
  '- Verify the view has no Permission \n';

stories
  .add(
    'View with applied Permission',
    () => (
      <PageSection>
        <ViewPermissionListItems
          key={0}
          viewId={'1'}
          viewName={'View_1'}
          index={0}
          i18nSelect={'Select'}
          i18nAddNewRole={'Add new role'}
          i18nRemoveRoleRow={'Remove role row'}
          i18nSelectARole={'select a role'}
          i18nSelectRole={'select role'}
          i18nRoleExists={'Role allready exists'}
          i18nInsert={'Insert'}
          i18nUpdate={'Update'}
          i18nDelete={'Delete'}
          i18nAllAccess={'All access'}
          i18nRole={'Permission role'}
          i18nShowLess={'ShowLess'}
          i18nPermissionNotSet={'Permission not set'}
          i18nCancel={'Cancel'}
          i18nSave={'Save'}
          i18nColumnMasking={'Column masking'}
          i18nColumnPermissions={'Column permissions'}
          i18nRowBasedFiltering={'Row-based filtering'}
          i18nEditPolicies={'Edit policies'}
          i18nEditPoliciesTip={'Edit policies tooltip'}
          i18nEditPoliciesTitle={'Edit policies of view View_1'}
          i18nAddPolicy={'Add policy'}
          i18nCondition={'Condition'}
          i18nOperation={'Operation'}
          i18nSelectOperation={'Select operation'}
          i18nUserRole={'User Role'}
          i18nValidate={'Validate'}
          i18nRemoveRow={'Remove row'}
          itemSelected={map}
          viewRolePermissionList={tablePrivileges}
          onSelectedViewChange={action(onSelectedViewChangeActionText)}
          dvRoles={['Developer']}
          getUpdatedRole={action('get updated permission')}
          // tslint:disable-next-line: jsx-no-lambda
          updateViewPolicies={() => Promise.resolve(true)}
          // tslint:disable-next-line: jsx-no-lambda
          updateViewsPermissions={() => Promise.resolve(true)}
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
          viewId={'1'}
          viewName={'View_1'}
          index={0}
          i18nSelect={'Select'}
          i18nAddNewRole={'Add new role'}
          i18nRemoveRoleRow={'Remove role row'}
          i18nSelectARole={'select a role'}
          i18nSelectRole={'select role'}
          i18nRoleExists={'Role allready exists'}
          i18nInsert={'Insert'}
          i18nUpdate={'Update'}
          i18nDelete={'Delete'}
          i18nAllAccess={'All access'}
          i18nRole={'Permission role'}
          i18nShowLess={'ShowLess'}
          i18nPermissionNotSet={'Permission not set'}
          i18nCancel={'Cancel'}
          i18nSave={'Save'}
          i18nColumnMasking={'Column masking'}
          i18nColumnPermissions={'Column permissions'}
          i18nRowBasedFiltering={'Row-based filtering'}
          i18nEditPolicies={'Edit policies'}
          i18nEditPoliciesTip={'Edit policies tooltip'}
          i18nEditPoliciesTitle={'Edit policies of view View_1'}
          i18nAddPolicy={'Add policy'}
          i18nCondition={'Condition'}
          i18nOperation={'Operation'}
          i18nSelectOperation={'Select operation'}
          i18nUserRole={'User Role'}
          i18nValidate={'Validate'}
          i18nRemoveRow={'Remove row'}
          itemSelected={map}
          viewRolePermissionList={tablePrivileges}
          onSelectedViewChange={action(onSelectedViewChangeActionText)}
          dvRoles={['Developer']}
          getUpdatedRole={action('get updated permission')}
          // tslint:disable-next-line: jsx-no-lambda
          updateViewPolicies={() => Promise.resolve(true)}
          // tslint:disable-next-line: jsx-no-lambda
          updateViewsPermissions={() => Promise.resolve(true)}
        />
      </PageSection>
    ),
    { notes: sampleViewPermissionListItemsNotes2 }
  );
