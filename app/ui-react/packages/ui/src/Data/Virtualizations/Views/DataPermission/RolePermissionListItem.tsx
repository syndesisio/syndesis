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
} from '@patternfly/react-core';
import { MinusCircleIcon } from '@patternfly/react-icons';
import * as React from 'react';

export interface IRolePermissionListItemProps {
  index: number;
  role: string[];
  selectedRole: string | undefined;
  selectedPermissions: string[];
  addRole: (roleName: string) => void;
  updateRolePermissionModel: (roleName: string, permissions: string[]) => void;
  removeRolePermission: (index: number) => void;
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

export const RolePermissionListItem: React.FunctionComponent<IRolePermissionListItemProps> = props => {
  const [checkState, setCheckState] = React.useState(
    getCheckInitial(props.selectedPermissions)
  );

  const [isExpanded, setIsExpanded] = React.useState<boolean>(false);
  const [selected, setSelected] = React.useState<any>(null);
  const [options, setOptions] = React.useState<IRoleOption[]>([]);
  const [showErrorAlert, setShowErrorAlert] = React.useState<boolean>(false);

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
    const existingOption = options.find(option => option.value === newValue);
    if (existingOption) {
      setShowErrorAlert(true);
      return;
    }
    props.addRole(newValue);
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

  React.useEffect(() => {
    // tslint:disable-next-line: no-unused-expression
    props.selectedRole && setSelected(props.selectedRole);
  }, [props.selectedRole]);

  React.useEffect(() => {
    const roleCopy = [...props.role];
    const roleList: IRoleOption[] = [];
    roleCopy.map(role => {
      roleList.push({ value: role });
    });
    setOptions(roleList);
  }, [props.role]);

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
    // tslint:disable-next-line: no-unused-expression
    selected &&
      props.updateRolePermissionModel(
        selected,
        getRolePermissionsModel(checkState)
      );
  }, [checkState, selected]);

  const titleId = 'role-select-id';

  return (
    <>
      {showErrorAlert && (
        <Alert
          variant={AlertVariant.warning}
          isInline={true}
          title="A role with this name already exists - using the existing role."
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
              <DataListCell key="primary content">
                <span id={titleId} hidden={true}>
                  Select a role
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
                  placeholderText="Select a role"
                  isCreatable={true}
                  onCreateOption={onCreateOption}
                >
                  {options.map((option, index) => (
                    <SelectOption key={index} value={option.value} />
                  ))}
                </Select>
              </DataListCell>,
              <DataListCell key="secondary content 1">
                <Checkbox
                  label=""
                  aria-label="uncontrolled checkbox example"
                  id="check-select"
                  name="selectCheck"
                  isChecked={checkState.selectCheck}
                  checked={checkState.selectCheck}
                  isDisabled={checkState.allAccessCheck || !selected}
                  onChange={handleChange}
                />
              </DataListCell>,
              <DataListCell key="secondary content 2">
                <Checkbox
                  label=""
                  aria-label="uncontrolled checkbox example"
                  id="check-insert"
                  name="insertCheck"
                  isChecked={checkState.insertCheck}
                  isDisabled={checkState.allAccessCheck || !selected}
                  onChange={handleChange}
                />
              </DataListCell>,
              <DataListCell key="secondary content 3">
                <Checkbox
                  label=""
                  aria-label="uncontrolled checkbox example"
                  id="check-update"
                  name="updateCheck"
                  isChecked={checkState.updateCheck}
                  isDisabled={checkState.allAccessCheck || !selected}
                  onChange={handleChange}
                />
              </DataListCell>,
              <DataListCell key="more content 4">
                <Checkbox
                  label=""
                  aria-label="uncontrolled checkbox example"
                  id="check-delete"
                  name="deleteCheck"
                  isChecked={checkState.deleteCheck}
                  isDisabled={checkState.allAccessCheck || !selected}
                  onChange={handleChange}
                />
              </DataListCell>,
              <DataListCell key="more content 5">
                <Checkbox
                  label=""
                  aria-label="uncontrolled checkbox example"
                  id="check-5"
                  name="allAccessCheck"
                  isChecked={checkState.allAccessCheck}
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
            <MinusCircleIcon
              // tslint:disable-next-line: jsx-no-lambda
              onClick={() => props.removeRolePermission(props.index)}
            />
          </DataListAction>
        </DataListItemRow>
      </DataListItem>
    </>
  );
};
