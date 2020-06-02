import {
  Alert,
  AlertActionCloseButton,
  AlertVariant,
  Checkbox,
  DataListAction,
  DataListCell,
  DataListItem,
  DataListItemCells,
  DataListItemRow,
  Select,
  SelectOption,
  SelectVariant,
  Tooltip,
  TooltipPosition,
} from '@patternfly/react-core';
import { MinusCircleIcon } from '@patternfly/react-icons';
import * as React from 'react';
import './RolePermissionListItem.css';

export interface IRolePermissionListItemProps {
  index: string;
  availableRoles: string[];
  roles: string[];
  selectedRole: string | undefined;
  selectedPermissions: string[];
  i18nSelectRole: string;
  i18nRemoveRoleRow: string;
  i18nRoleExists: string;
  updateRolePermissionModel: (
    roleName: string | undefined,
    permissions: string[],
    deleteRole: boolean,
    prevSelected: string | undefined
  ) => void;
  deleteRoleFromPermissionModel: (roleName: string) => void;
  removeRolePermission: (index: string) => void;
}

export interface IRoleOption {
  value: string;
}
const getCheckInitial = (selectedPermissions: string[]) => {
  const newState = {
    allAccessCheck: false,
    deleteCheck: false,
    insertCheck: false,
    selectCheck: false,
    updateCheck: false,
  };
  for (const permission of selectedPermissions) {
    if (permission === 'SELECT') {
      newState.selectCheck = true;
    } else if (permission === 'INSERT') {
      newState.insertCheck = true;
    } else if (permission === 'UPDATE') {
      newState.updateCheck = true;
    } else if (permission === 'DELETE') {
      newState.deleteCheck = true;
    }
  }
  return newState;
};

const getPermission = (permission: string): string[] => {
  switch (permission) {
    case 'selectCheck':
      return ['SELECT'];
    case 'insertCheck':
      return ['INSERT'];
    case 'updateCheck':
      return ['UPDATE'];
    case 'deleteCheck':
      return ['DELETE'];
    default:
      return ['SELECT', 'INSERT', 'UPDATE', 'DELETE'];
  }
};

const getRolePermissionsModel = (checkState: any) => {
  let returnVal: string[] = [];
  if (checkState.allAccessCheck) {
    return getPermission('allAccessCheck');
  } else {
    if (checkState.selectCheck) {
      returnVal = [...returnVal, ...getPermission('selectCheck')];
    }
    if (checkState.insertCheck) {
      returnVal = [...returnVal, ...getPermission('insertCheck')];
    }
    if (checkState.updateCheck) {
      returnVal = [...returnVal, ...getPermission('updateCheck')];
    }
    if (checkState.deleteCheck) {
      returnVal = [...returnVal, ...getPermission('deleteCheck')];
    }
    return returnVal;
  }
};

const getRoles = (roles: string[]) =>{
    const roleList: IRoleOption[] = [];
    roles.map((role: any) => {
      roleList.push({ value: role });
    });
    return roleList;
}

export const RolePermissionListItem: React.FunctionComponent<IRolePermissionListItemProps> = React.memo( props => {
  const [checkState, setCheckState] = React.useState(
    getCheckInitial(props.selectedPermissions)
  );

  const [isExpanded, setIsExpanded] = React.useState<boolean>(false);
  const [selected, setSelected] = React.useState<any>(null);
  const [options, setOptions] = React.useState<IRoleOption[]>(getRoles(props.availableRoles));
  const [showErrorAlert, setShowErrorAlert] = React.useState<boolean>(false);

  const prevSelectedRef = React.useRef();
  React.useEffect(() => {
    prevSelectedRef.current = selected;
  });
  const prevSelected: string | undefined = prevSelectedRef.current;

  // tslint:disable-next-line: no-shadowed-variable
  const onToggle = (isExpanded: boolean) => {
    setIsExpanded(isExpanded);
  };

  const onSelect = (event: any, selection: any, isPlaceholder: any) => {
    if (isPlaceholder) {
      clearSelection();
    } else {
      setIsExpanded(false);
      setSelected(selection);
    }
  };

  const onCreateOption = (newValue: string) => {
    // determine if requested rolename already exists
    const existingRole = props.roles.find(role => role.toLowerCase() === newValue.toLowerCase());
    if (existingRole) {
      setShowErrorAlert(true);
      return;
    }
  };

  const clearSelection = () => {
    setIsExpanded(false);
    setSelected(null);
  };

  const handleChange = (checked: boolean, event: any) => {
    const target = event.target;
    const value = target.type === 'checkbox' ? target.checked : target.value;
    const name = target.name;
    const newState = { ...checkState };
    newState[name] = value;
    setCheckState(newState);
  };

  const removePermissionRow = () => {
    props.removeRolePermission(props.index);
    // tslint:disable-next-line: no-unused-expression
    selected && props.deleteRoleFromPermissionModel(selected);
  };

  React.useEffect(() => {
    // tslint:disable-next-line: no-unused-expression
    props.selectedRole && setSelected(props.selectedRole);
  }, [props.selectedRole]);

  React.useEffect(() => {
    const roleCopy = [...props.availableRoles];
    const roleList: IRoleOption[] = [];
    roleCopy.map(role => {
      roleList.push({ value: role });
    });
    setOptions(roleList);
  }, [props.availableRoles]);

  React.useEffect(() => {
    const newState = { ...checkState };
    if (checkState.allAccessCheck !== null) {
      newState.selectCheck = checkState.allAccessCheck;
      newState.insertCheck = checkState.allAccessCheck;
      newState.updateCheck = checkState.allAccessCheck;
      newState.deleteCheck = checkState.allAccessCheck;
      setCheckState(newState);
    }
  }, [checkState.allAccessCheck]);

  React.useEffect(() => {
    const newState = { ...checkState };
    newState.allAccessCheck =
      checkState.selectCheck &&
      checkState.insertCheck &&
      checkState.updateCheck &&
      checkState.deleteCheck;
    setCheckState(newState);
  }, [
    checkState.selectCheck,
    checkState.insertCheck,
    checkState.updateCheck,
    checkState.deleteCheck,
  ]);

  React.useEffect(() => {
    const deleteRole = selected !== prevSelected;
    // tslint:disable-next-line: no-unused-expression
    props.updateRolePermissionModel(
      selected,
      getRolePermissionsModel(checkState),
      deleteRole,
      prevSelected
    );
  }, [checkState, selected]);

  const titleId = 'role-select-id';

  return (
    <>
      {showErrorAlert && (
        <Alert
          variant={AlertVariant.warning}
          isInline={true}
          title={props.i18nRoleExists}
          action={
            // tslint:disable-next-line: jsx-no-lambda
            <AlertActionCloseButton onClose={() => setShowErrorAlert(false)} />
          }
        />
      )}
      <DataListItem aria-labelledby="single-action-item1">
        <DataListItemRow>
          <DataListItemCells
            dataListCells={[
              <DataListCell key="roleSelect" width={2}>
                <span id={titleId} hidden={true}>
                  {props.i18nSelectRole}
                </span>
                <Select
                  variant={SelectVariant.typeahead}
                  ariaLabelTypeAhead="Select a state"
                  onToggle={onToggle}
                  onSelect={onSelect}
                  onClear={clearSelection}
                  selections={selected}
                  isExpanded={isExpanded}
                  ariaLabelledBy={titleId}
                  placeholderText={props.i18nSelectRole}
                  isCreatable={true}
                  onCreateOption={onCreateOption}
                  className={'role-permission-list-item_select'}
                >
                  {options.map((option, index) => (
                    <SelectOption key={index} value={option.value} />
                  ))}
                </Select>
              </DataListCell>,
              <DataListCell key="selectCheckbox" width={1}>
                <Checkbox
                  label=""
                  className="role-permission-list-item_checkbox"
                  aria-label="select checkbox"
                  id="check-select"
                  name="selectCheck"
                  isChecked={checkState.selectCheck}
                  checked={checkState.selectCheck}
                  isDisabled={checkState.allAccessCheck || !selected}
                  onChange={handleChange}
                />
              </DataListCell>,
              <DataListCell key="insertCheckbox" width={1}>
                <Checkbox
                  label=""
                  className="role-permission-list-item_checkbox"
                  aria-label="insert checkbox"
                  id="check-insert"
                  name="insertCheck"
                  isChecked={checkState.insertCheck}
                  isDisabled={checkState.allAccessCheck || !selected}
                  onChange={handleChange}
                />
              </DataListCell>,
              <DataListCell key="updateCheckbox" width={1}>
                <Checkbox
                  label=""
                  className="role-permission-list-item_checkbox"
                  aria-label="update checkbox"
                  id="check-update"
                  name="updateCheck"
                  isChecked={checkState.updateCheck}
                  isDisabled={checkState.allAccessCheck || !selected}
                  onChange={handleChange}
                />
              </DataListCell>,
              <DataListCell key="deleteCheckbox" width={1}>
                <Checkbox
                  label=""
                  className="role-permission-list-item_checkbox"
                  aria-label="delete checkbox"
                  id="check-delete"
                  name="deleteCheck"
                  isChecked={checkState.deleteCheck}
                  isDisabled={checkState.allAccessCheck || !selected}
                  onChange={handleChange}
                />
              </DataListCell>,
              <DataListCell key="allAccessCheckbox" width={1}>
                <Checkbox
                  label=""
                  className="role-permission-list-item_checkbox"
                  aria-label="allAccess checkbox"
                  id="check-5"
                  name="allAccessCheck"
                  isChecked={checkState.allAccessCheck}
                  isDisabled={!selected}
                  onChange={handleChange}
                />
              </DataListCell>,
            ]}
          />
          <DataListAction
            aria-labelledby="single-action-item1 single-action-action1"
            id="single-action-action1"
            aria-label="Actions"
          >
            <Tooltip
              position={TooltipPosition.top}
              content={<div>{props.i18nRemoveRoleRow}</div>}
            >
              <MinusCircleIcon
                className={'role-permission-list-item_remove-button'}
                onClick={removePermissionRow}
              />
            </Tooltip>
          </DataListAction>
        </DataListItemRow>
      </DataListItem>
    </>
  );
});
