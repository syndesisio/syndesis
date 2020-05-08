import {
  Button,
  DataList,
  DataListAction,
  DataListCell,
  DataListItem,
  DataListItemCells,
  DataListItemRow,
} from '@patternfly/react-core';
import { PlusCircleIcon } from '@patternfly/react-icons';
import * as React from 'react';
import { ITablePrivilege, RolePermissionListItem } from '..';
import './RolePermissionList.css';

export interface IRolePermissionListProps {
  i18nRole: string;
  i18nSelect: string;
  i18nInsert: string;
  i18nUpdate: string;
  i18nDelete: string;
  i18nAllAccess: string;
  i18nAddNewRole: string;
  viewRolePermissionList: ITablePrivilege[];
  updateRolePermissionModel: (roleName: string, permissions: string[]) => void;
  roles: string[];
}

export const RolePermissionList: React.FunctionComponent<IRolePermissionListProps> = props => {
  const [roleRowList, setRoleRowList] = React.useState<JSX.Element[]>([]);
  const [currentRoles, setCurrentRoles] = React.useState<string[]>(props.roles);

  const addRole = (roleName: string) => {
    setCurrentRoles([...currentRoles, roleName]);
  };

  const removeRolePermission = (index: number) => {
    setRoleRowList([
      ...roleRowList.slice(0, index),
      ...roleRowList.slice(index + 1),
    ]);
  };

  const addRolePermission = () => {
    setRoleRowList([
      ...roleRowList,
      <RolePermissionListItem
        index={roleRowList.length}
        role={currentRoles}
        addRole={addRole}
        removeRolePermission={removeRolePermission}
        selectedRole=""
        selectedPermissions={[]}
        updateRolePermissionModel={props.updateRolePermissionModel}
        key={`rolelist-${roleRowList.length}`}
      />,
    ]);
  };

  return (
    <>
      <DataList aria-label=" and action data list example">
        <DataListItem aria-labelledby="check-action-item1">
          <DataListItemRow>
            <DataListItemCells
              dataListCells={[
                <DataListCell key="role label">
                  <b>{props.i18nRole}</b>
                </DataListCell>,
                <DataListCell key="select label">
                  <b>{props.i18nSelect}</b>
                </DataListCell>,
                <DataListCell key="insert label">
                  <b>{props.i18nInsert}</b>
                </DataListCell>,
                <DataListCell key="update label">
                  <b>{props.i18nUpdate}</b>
                </DataListCell>,
                <DataListCell key="delete label">
                  <b>{props.i18nDelete}</b>
                </DataListCell>,
                <DataListCell key="allaccess label">
                  <b>{props.i18nAllAccess}</b>
                </DataListCell>,
              ]}
            />
            <DataListAction
              aria-labelledby="single-action-item1 single-action-action1"
              id="single-action-action1"
              aria-label="Actions"
              className={'role-permission-list_heading'}
            >
              ""
            </DataListAction>
          </DataListItemRow>
        </DataListItem>
        {roleRowList.map((element: JSX.Element) => element)}
      </DataList>
      <Button
        variant="link"
        icon={<PlusCircleIcon />}
        onClick={addRolePermission}
      >
        {props.i18nAddNewRole}
      </Button>
    </>
  );
};
