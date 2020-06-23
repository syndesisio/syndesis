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
import { ITablePrivilege, RolePermissionListItem } from '.';
import './RolePermissionList.css';

export interface IRolePermissionListProps {
  i18nRole: string;
  i18nSelect: string;
  i18nInsert: string;
  i18nUpdate: string;
  i18nDelete: string;
  i18nAllAccess: string;
  i18nAddNewRole: string;
  i18nSelectRole: string;
  i18nRemoveRoleRow: string;
  i18nRoleExists: string;
  viewRolePermissionList: ITablePrivilege[];
  selectedRoles: Map<string, string[]>;
  clearAction: boolean;
  updateRolePermissionModel: (
    roleName: string | undefined,
    permissions: string[],
    deleteRole: boolean,
    prevSelected: string | undefined
  ) => void;
  deleteRoleFromPermissionModel: (roleName: string) => void;
  roles: string[];
}

const getRandomId = () => {
  return 'role' + Math.floor(Math.random() * 100000);
};

export const RolePermissionList: React.FunctionComponent<IRolePermissionListProps> = props => {
  const [roleRowList, setRoleRowList] = React.useState<Map<string, any>>(
    new Map<string, any>()
  );
  const [availableRoles, setAvailableRoles] = React.useState<string[]>(
    props.roles
  );

  const removeRolePermission = React.useCallback(
    index => {
      const rolelistCopy = new Map<string, any>(roleRowList);
      rolelistCopy.delete(index);
      setRoleRowList(rolelistCopy);
    },
    [roleRowList, setRoleRowList]
  );

  const addRolePermission = () => {
    const rolelistCopy = new Map<string, any>(roleRowList);
    rolelistCopy.set(getRandomId(), {});
    setRoleRowList(rolelistCopy);
  };

  const setAppliedPermissions = () => {
    let rolelistCopy = new Map<string, any>();
    for (const permissions of props.viewRolePermissionList) {
      rolelistCopy.set(getRandomId(), permissions || {});
    }
    if (props.viewRolePermissionList.length === 0) {
      rolelistCopy = new Map<string, any>();
      rolelistCopy.set(getRandomId(), {});
    }
    setRoleRowList(rolelistCopy);
  };

  React.useEffect(() => {
    const updatedRoles = props.roles.filter(role => {
      return !props.selectedRoles.has(role);
    });
    setAvailableRoles(updatedRoles);
  }, [props.roles, props.selectedRoles]);

  React.useEffect(() => {
    if (props.viewRolePermissionList.length === 0 && roleRowList.size === 0) {
      const rolelistCopy = new Map<string, any>();
      rolelistCopy.set(getRandomId(), {});
      setRoleRowList(rolelistCopy);
    } else {
      setAppliedPermissions();
    }
  }, [props.viewRolePermissionList]);

  React.useEffect(() => {
    setAppliedPermissions();
  }, [props.clearAction]);

  return (
    <>
      <DataList aria-label=" and action data list example">
        <DataListItem aria-labelledby="check-action-item1">
          <DataListItemRow>
            <DataListItemCells
              dataListCells={[
                <DataListCell key="role label" width={2}>
                  <b>{props.i18nRole}</b>
                </DataListCell>,
                <DataListCell key="select label" width={1}>
                  <b>{props.i18nSelect}</b>
                </DataListCell>,
                <DataListCell key="insert label" width={1}>
                  <b>{props.i18nInsert}</b>
                </DataListCell>,
                <DataListCell key="update label" width={1}>
                  <b>{props.i18nUpdate}</b>
                </DataListCell>,
                <DataListCell key="delete label" width={1}>
                  <b>{props.i18nDelete}</b>
                </DataListCell>,
                <DataListCell key="allaccess label" width={1}>
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
        {Array.from(roleRowList.entries()).map(([key, value]) => {
          return (
            <RolePermissionListItem
              index={key}
              availableRoles={availableRoles}
              roles={props.roles}
              removeRolePermission={removeRolePermission}
              selectedRole={value.roleName || ''}
              selectedPermissions={value.grantPrivileges || []}
              updateRolePermissionModel={props.updateRolePermissionModel}
              deleteRoleFromPermissionModel={
                props.deleteRoleFromPermissionModel
              }
              i18nSelectRole={props.i18nSelectRole}
              i18nRemoveRoleRow={props.i18nRemoveRoleRow}
              i18nRoleExists={props.i18nRoleExists}
              key={key}
            />
          );
        })}
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
