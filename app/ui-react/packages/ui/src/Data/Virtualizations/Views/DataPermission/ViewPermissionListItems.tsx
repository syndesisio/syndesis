import {
  Badge,
  Button,
  DataListCell,
  DataListCheck,
  DataListItem,
  DataListItemCells,
  DataListItemRow,
} from '@patternfly/react-core';
import * as React from 'react';
import { ITablePrivilege } from '..';
// import { RolePermissionList } from './RolePermissionList';
import './ViewPermissionListItems.css';

export interface IViewPermissionListItemsProps {
  // i18nAddNewRole: string;
  // i18nSelect: string;
  // i18nInsert: string;
  // i18nUpdate: string;
  // i18nDelete: string;
  // i18nAllAccess: string;
  // i18nRole: string;
  viewId: string;
  viewName: string;
  viewRolePermissionList: ITablePrivilege[];
  itemSelected: Map<string, string>;
  i18nPermissionNotSet: string;
  // status: any;
  // dvRoles: string[];
  onSelectedViewChange: (
    checked: boolean,
    event: any,
    viewName: string,
    viewId: string
  ) => void;
}

export const ViewPermissionListItems: React.FC<IViewPermissionListItemsProps> = props => {
  /**
   * React useState Hook to handle state in component.
   */
  // const [show, setShow] = React.useState<boolean>(false);

  const [trimPermissionList, setTrimPermissionList] = React.useState<
    ITablePrivilege[]
  >([]);

  // const [rolePermissionModel,setRolePermissionModel ] = React.useState<Map<string, string[]>>( new Map());

  const [showAll, setShowAll] = React.useState<boolean>(false);

  // const updateRolePermissionModel = (roleName: string, permissions: string[]) => {
  //   const rolePermissionModelCopy = new Map(rolePermissionModel);
  //   setRolePermissionModel(rolePermissionModelCopy.set(roleName,permissions));
  // }

  React.useEffect(() => {
    if (props.viewRolePermissionList.length > 4) {
      const copyList = props.viewRolePermissionList.slice();
      copyList.length = 4;
      setTrimPermissionList(copyList);
    } else {
      setTrimPermissionList([]);
    }
  },[props.viewRolePermissionList]);

  return (
    <DataListItem
      aria-labelledby="width-ex3-item1"
      // isExpanded={show}
    >
      <DataListItemRow>
        {/* <DataListToggle
          isExpanded={show}
          id="width-ex3-toggle1"
          aria-controls="width-ex3-expand1"
          // tslint:disable-next-line: jsx-no-lambda
          onClick={() => setShow(!show)}
        /> */}
        <DataListCheck
          aria-labelledby="width-ex3-item1"
          name="width-ex3-item1"
          checked={Array.from(props.itemSelected.values()).includes(
            props.viewName
          )}
          // tslint:disable-next-line: jsx-no-lambda
          onChange={(checked: boolean, event: any) =>
            props.onSelectedViewChange(
              checked,
              event,
              props.viewName,
              props.viewId
            )
          }
        />
        <DataListItemCells
          dataListCells={[
            <DataListCell width={1} key={props.viewId}>
              <span id="check-action-item2">{props.viewName}</span>
            </DataListCell>,
            <DataListCell width={5} key={`temp-${props.viewId}`}>
              {props.viewRolePermissionList.length > 4 && !showAll
                ? trimPermissionList.map((permissionSet, index) => (
                    <Badge
                      key={`temp2-${index}`}
                      isRead={true}
                      className={'view-permission-list-items-permission_badge'}
                    >
                      {permissionSet.roleName +
                        ' : ' +
                        permissionSet.grantPrivileges.join(' / ')}
                    </Badge>
                  ))
                : props.viewRolePermissionList.map((permissionSet, index) => (
                    <Badge
                      key={`temp2-${index}`}
                      isRead={true}
                      className={'view-permission-list-items-permission_badge'}
                    >
                      {permissionSet.roleName +
                        ' : ' +
                        permissionSet.grantPrivileges.join(' / ')}
                    </Badge>
                  ))}
              {trimPermissionList.length > 0 && (
                // tslint:disable-next-line: jsx-no-lambda
                <Button variant={'link'} onClick={() => setShowAll(!showAll)}>
                  {showAll
                    ? 'Show less'
                    : `${props.viewRolePermissionList.length - 4}more...`}{' '}
                </Button>
              )}
              {props.viewRolePermissionList.length === 0 && (
                <span className={'view-permission-list-items-disabled_text'}>
                  <i>{props.i18nPermissionNotSet}</i>
                </span>
              )}
            </DataListCell>,
          ]}
        />
      </DataListItemRow>
      {/* <DataListContent
        aria-label="Primary Content Details"
        id="width-ex3-expand1"
        isHidden={!show}
      >
        <>
          {props.status.ssoConfigured === 'false' && (
            <Alert
              variant={AlertVariant.warning}
              isInline={true}
              title="SSO not configured: Edited role won't be used for publishishing until sso is configured."
            />
          )}
          <RolePermissionList
            i18nRole={props.i18nRole}
            i18nSelect={props.i18nSelect}
            i18nInsert={props.i18nInsert}
            i18nUpdate={props.i18nUpdate}
            i18nDelete={props.i18nDelete}
            i18nAllAccess={props.i18nAllAccess}
            i18nAddNewRole={props.i18nAddNewRole}
            viewRolePermissionList={props.viewRolePermissionList}
            updateRolePermissionModel={updateRolePermissionModel}
            roles={props.dvRoles}
          />
        </>
      </DataListContent> */}
    </DataListItem>
  );
};
