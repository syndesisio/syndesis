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
  i18nSelectRole: string;
  i18nRemoveRoleRow: string;
  i18nRoleExists: string;
  viewRolePermissionList: ITablePrivilege[];
  selectedRoles: Map<string, string[]>;
  updateRolePermissionModel: (
    roleName: string | undefined,
    permissions: string[],
    deleteRole: boolean,
    prevSelected: string | undefined
  ) => void;
  deleteRoleFromPermissionModel: (roleName: string) => void;
  roles: string[];
}

const selectedPermissions: string[] = [];

export const RolePermissionList: React.FunctionComponent<IRolePermissionListProps> = props => {

  const [roleRowList, setRoleRowList] = React.useState<string[]>(['role0']);
  const [currentRoles, setCurrentRoles] = React.useState<string[]>(props.roles);

  const removeRolePermission = React.useCallback(
    (index) => {
      const rolelistCopy = roleRowList.slice();
      rolelistCopy.splice(rolelistCopy.indexOf(index), 1);
      setRoleRowList(rolelistCopy);
    },
    [roleRowList, setRoleRowList],
  );


  const addRolePermission = () => {
    if (roleRowList.length === 0) {
      setRoleRowList(['role0']);
    } else {
      const lastRowId = roleRowList[roleRowList.length - 1];
      const roleRowNo = +lastRowId.substring(4, lastRowId.length) + 1;
      setRoleRowList([...roleRowList, 'role' + roleRowNo]);
    }
  };
  React.useEffect(()=>{
    setCurrentRoles(props.roles);
  },[props.roles]);

  React.useEffect(() => {
    const updatedRoles = props.roles.filter(role => {
      return !props.selectedRoles.has(role);
    });
    setCurrentRoles(updatedRoles);
  }, [props.selectedRoles]);

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
        {roleRowList.map((roleNumber: string) => (
          <RolePermissionListItem
            index={roleNumber}
            availableRoles={currentRoles}
            roles={props.roles}
            removeRolePermission={removeRolePermission}
            selectedRole=""
            selectedPermissions={selectedPermissions}
            updateRolePermissionModel={props.updateRolePermissionModel}
            deleteRoleFromPermissionModel={props.deleteRoleFromPermissionModel}
            i18nSelectRole={props.i18nSelectRole}
            i18nRemoveRoleRow={props.i18nRemoveRoleRow}
            i18nRoleExists={props.i18nRoleExists}
            key={roleNumber}
          />
        ))}
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
