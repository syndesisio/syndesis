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
import { RolePermissionListItem } from '..';

export interface IRolePermissionListProps {
  i18nRole: string;
  i18nRead: string;
  i18nEdit: string;
  i18nDelete: string;
  i18nAllAccess: string;
  i18nAddNewRole: string;
}

export const RolePermissionList: React.FunctionComponent<IRolePermissionListProps> = props => {
  const [roleRowList, setRoleRowList] = React.useState<JSX.Element[]>([]);

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
        removeRolePermission={removeRolePermission}
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
                <DataListCell key="primary content">
                  <b>{props.i18nRole}</b>
                </DataListCell>,
                <DataListCell key="secondary content 1">
                  <b>{props.i18nRead}</b>
                </DataListCell>,
                <DataListCell key="secondary content 2">
                  <b>{props.i18nEdit}</b>
                </DataListCell>,
                <DataListCell key="more content 1">
                  <b>{props.i18nDelete}</b>
                </DataListCell>,
                <DataListCell key="more content 2">
                  <b>{props.i18nAllAccess}</b>
                </DataListCell>,
              ]}
            />
            <DataListAction
              aria-labelledby="single-action-item1 single-action-action1"
              id="single-action-action1"
              aria-label="Actions"
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
