import {
  Checkbox,
  DataListAction,
  DataListCell,
  DataListItem,
  DataListItemCells,
  DataListItemRow,
} from '@patternfly/react-core';
import { MinusCircleIcon } from '@patternfly/react-icons';
import * as React from 'react';

export interface IRolePermissionListItemProps {
  index: number;
  removeRolePermission: (index: number) => void;
}

export const RolePermissionListItem: React.FunctionComponent<IRolePermissionListItemProps> = props => {
  const [checkState, setCheckState] = React.useState({
    allAccessCheck: false,
    deleteCheck: false,
    editCheck: false,
    readCheck: false,
  });

  const handleChange = (checked: boolean, event: any) => {
    const target = event.target;
    const value = target.type === 'checkbox' ? target.checked : target.value;
    const name = target.name;
    const newState = { ...checkState };
    newState[name] = value;
    setCheckState(newState);
  };

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
  return (
    <DataListItem aria-labelledby="single-action-item1">
      <DataListItemRow>
        <DataListItemCells
          dataListCells={[
            <DataListCell key="primary content">
              Developer {props.index}
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
            onClick={() => props.removeRolePermission(props.index)}
          />
        </DataListAction>
      </DataListItemRow>
    </DataListItem>
  );
};
