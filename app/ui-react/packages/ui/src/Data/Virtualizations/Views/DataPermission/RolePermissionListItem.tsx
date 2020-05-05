import {
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
  removeRolePermission: (index: number) => void;
}

export interface IRoleOption {
  value: string;
}

export const RolePermissionListItem: React.FunctionComponent<IRolePermissionListItemProps> = props => {
  const [checkState, setCheckState] = React.useState({
    allAccessCheck: false,
    deleteCheck: false,
    editCheck: false,
    readCheck: false,
  });

  const [isExpanded, setIsExpanded] = React.useState<boolean>(false);
  const [selected, setSelected] = React.useState<any>(null);
  const [options, setOptions] = React.useState<IRoleOption[]>([])


  // tslint:disable-next-line: no-shadowed-variable
  const onToggle = (isExpanded: boolean) => {
    setIsExpanded(isExpanded);
  };

  const onSelect = (event: any, selection: any, isPlaceholder: any) => {
    if (isPlaceholder){
      clearSelection();
    } 
    else {
 
      setIsExpanded(false);
      setSelected(selection)
    }
  };

  const onCreateOption = (newValue: string) => {
    setOptions([...options, { value: newValue }]);
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

  React.useEffect(()=>{
    const roleCopy = [...props.role];
    const roleList: IRoleOption[] = [];
    roleCopy.map(role =>{
      roleList.push({value: role})
    });
    setOptions(roleList);
  },[props.role])

  React.useEffect(() => {
    const newState = { ...checkState };
    if (checkState.allAccessCheck !== null) {
      newState.readCheck = checkState.allAccessCheck;
      newState.editCheck = checkState.allAccessCheck;
      newState.deleteCheck = checkState.allAccessCheck;
      setCheckState(newState);
    }
  }, [checkState.allAccessCheck]);

  React.useEffect(() => {
    const newState = { ...checkState };
    newState.allAccessCheck =
      checkState.readCheck && checkState.editCheck && checkState.deleteCheck;
    setCheckState(newState);
  }, [checkState.readCheck, checkState.editCheck, checkState.deleteCheck]);

  const titleId = 'role-select-id';

  return (
    <DataListItem aria-labelledby="single-action-item1">
      <DataListItemRow>
        <DataListItemCells
          dataListCells={[
            <DataListCell key="primary content">
              <span id={titleId} hidden={true}>
          Select a state
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
                id="check-read"
                name="readCheck"
                isChecked={checkState.readCheck}
                isDisabled={checkState.allAccessCheck}
                onChange={handleChange}
              />
            </DataListCell>,
            <DataListCell key="secondary content 2">
              <Checkbox
                label=""
                aria-label="uncontrolled checkbox example"
                id="check-edit"
                name="editCheck"
                isChecked={checkState.editCheck}
                isDisabled={checkState.allAccessCheck}
                onChange={handleChange}
              />
            </DataListCell>,
            <DataListCell key="more content 1">
              <Checkbox
                label=""
                aria-label="uncontrolled checkbox example"
                id="check-delete"
                name="deleteCheck"
                isChecked={checkState.deleteCheck}
                isDisabled={checkState.allAccessCheck}
                onChange={handleChange}
              />
            </DataListCell>,
            <DataListCell key="more content 2">
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
  );
};
