import {
  Badge,
  Button,
  DataListCell,
  DataListCheck,
  DataListContent,
  DataListItem,
  DataListItemCells,
  DataListItemRow,
  DataListToggle,
  Radio,
  Split,
  SplitItem,
} from '@patternfly/react-core';
import * as React from 'react';
import { IRoleInfo, ITablePrivilege, RolePermissionList } from '..';
import { ButtonLink, Loader } from '../../../../Layout';
import './ViewPermissionListItems.css';

export interface IViewPermissionListItemsProps {
  index: number;
  i18nAddNewRole: string;
  i18nSelect: string;
  i18nSelectRole: string;
  i18nRemoveRoleRow: string;
  i18nRoleExists: string;
  i18nInsert: string;
  i18nUpdate: string;
  i18nDelete: string;
  i18nAllAccess: string;
  i18nRole: string;
  i18nPermissionNotSet: string;
  i18nShowLess: string;
  i18nCancel: string;
  i18nSave: string;
  viewId: string;
  viewName: string;
  viewRolePermissionList: ITablePrivilege[];
  itemSelected: Map<string, string>;
  dvRoles: string[];
  updateViewsPermissions: (roleInfo: IRoleInfo) => Promise<boolean>;
  getUpdatedRole: () => void;
  onSelectedViewChange: (
    checked: boolean,
    event: any,
    viewName: string,
    viewId: string
  ) => void;
}

const getUpdatePermissionsPayload = (
  permissionsModel: Map<string, string[]>,
  viewSelected: string
) => {
  const returnVal: ITablePrivilege[] = [];
  permissionsModel.forEach((value: string[], key: string) => {
    returnVal.push({
      grantPrivileges: value,
      roleName: key,
      viewDefinitionIds: [viewSelected],
    });
  });

  return returnVal;
};

export const ViewPermissionListItems: React.FC<IViewPermissionListItemsProps> = props => {
  /**
   * React useState Hook to handle state in component.
   */
  const [show, setShow] = React.useState<boolean>(false);

  const [showLoading, setShowLoading] = React.useState<boolean>(false);

  const [grantOperation, setGrantOperation] = React.useState<boolean>(true);

  const [trimPermissionList, setTrimPermissionList] = React.useState<
    ITablePrivilege[]
  >([]);

  const [rolePermissionModel, setRolePermissionModel] = React.useState<
    Map<string, string[]>
  >(new Map());

  const [showAll, setShowAll] = React.useState<boolean>(false);
  const [saveEnabled, setSaveEnabled] = React.useState<boolean>(false);

  const [clearAction, setClearAction] = React.useState<boolean>(false);

  const updateRolePermissionModel = React.useCallback(
    (
      roleName: string | undefined,
      permissions: string[],
      deleteRole: boolean,
      prevSelected: string | undefined
    ) => {
      const rolePermissionModelCopy = new Map<string, string[]>(
        rolePermissionModel
      );
      // tslint:disable-next-line: no-unused-expression
      roleName && rolePermissionModelCopy.set(roleName, permissions);
      if (deleteRole && prevSelected) {
        rolePermissionModelCopy.delete(prevSelected);
      }
      setRolePermissionModel(rolePermissionModelCopy);
    },
    [rolePermissionModel, setRolePermissionModel]
  );

  const deleteRoleFromPermissionModel = React.useCallback(
    (roleName: string) => {
      const rolePermissionModelCopy = new Map<string, string[]>(
        rolePermissionModel
      );
      // tslint:disable-next-line: no-unused-expression
      rolePermissionModelCopy.delete(roleName) &&
        setRolePermissionModel(rolePermissionModelCopy);
    },
    [rolePermissionModel, setRolePermissionModel]
  );

  const clearRolePermissionModel = () => {
    setRolePermissionModel(new Map<string, string[]>());
  };

  const handleCancel = () => {
    clearRolePermissionModel();
    setGrantOperation(true);
    setClearAction(!clearAction);
    setShow(!show);
  };

  const handleUpdateRoles = async () => {
    setShowLoading(true);
    const callSucess = await props.updateViewsPermissions({
      operation: grantOperation ? 'GRANT' : 'REVOKE',
      tablePrivileges: getUpdatePermissionsPayload(
        rolePermissionModel,
        props.viewId
      ),
    });
    if (callSucess) {
      props.getUpdatedRole();
      setShowLoading(false);
      clearRolePermissionModel();
      setGrantOperation(true);
    }
  };

  const setBulkRolePermissionModel = () => {
    const rolePermissionModelCopy = new Map<string, string[]>();
    for (const permissions of props.viewRolePermissionList) {
      // tslint:disable-next-line: no-unused-expression
      permissions.roleName &&
        rolePermissionModelCopy.set(
          permissions.roleName,
          permissions.grantPrivileges
        );
    }
    setRolePermissionModel(rolePermissionModelCopy);
  };

  React.useEffect(() => {
    if (props.viewRolePermissionList.length > 4) {
      const copyList = props.viewRolePermissionList.slice();
      copyList.length = 4;
      setTrimPermissionList(copyList);
    } else {
      setTrimPermissionList([]);
    }
    setBulkRolePermissionModel();
  }, [props.viewRolePermissionList]);

  React.useEffect(() => {
    if (rolePermissionModel.size < 1 || showLoading) {
      setSaveEnabled(false);
      // Save button enables if one or more roles exist, and all have a permission checked
    } else if (rolePermissionModel.size > 0) {
      let allHaveSelection = true;
      for (const entry of Array.from(rolePermissionModel.entries())) {
        if (entry[1].length === 0) {
          allHaveSelection = false;
          break;
        }
      }
      setSaveEnabled(allHaveSelection);
    } else {
      setSaveEnabled(false);
    }
  }, [rolePermissionModel, showLoading]);

  return (
    <DataListItem aria-labelledby="width-ex3-item1" isExpanded={show}>
      <DataListItemRow>
        <DataListToggle
          isExpanded={show}
          id="width-ex3-toggle1"
          aria-controls="width-ex3-expand1"
          // tslint:disable-next-line: jsx-no-lambda
          onClick={() => setShow(!show)}
        />
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
                    ? `${props.i18nShowLess}`
                    : `${props.viewRolePermissionList.length - 4} more...`}{' '}
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
      <DataListContent
        aria-label="Primary Content Details"
        id="width-ex3-expand1"
        isHidden={!show}
      >
        <>
          <Split gutter="sm" className={'view-permission-list_set-model_grant'}>
            <SplitItem>
              <Radio
                aria-label={'View-grant'}
                id={'view-operation-grant' + props.index}
                data-testid={'view-operation-grant' + props.index}
                name={'view-operation' + props.index}
                label="GRANT"
                // tslint:disable-next-line: jsx-no-lambda
                onClick={() => setGrantOperation(true)}
                isChecked={grantOperation}
              />
            </SplitItem>
            <SplitItem>
              <Radio
                aria-label={'view-revoke'}
                id={'view-operation-revoke' + props.index}
                className={'view-permission-list_radios' + props.index}
                data-testid={'view-operation-revoke' + props.index}
                name={'view-operation' + props.index}
                label="REVOKE"
                // tslint:disable-next-line: jsx-no-lambda
                onClick={() => setGrantOperation(false)}
                isChecked={!grantOperation}
              />
            </SplitItem>
          </Split>
          <RolePermissionList
            i18nRole={props.i18nRole}
            i18nSelect={props.i18nSelect}
            i18nInsert={props.i18nInsert}
            i18nUpdate={props.i18nUpdate}
            i18nDelete={props.i18nDelete}
            i18nAllAccess={props.i18nAllAccess}
            i18nAddNewRole={props.i18nAddNewRole}
            i18nSelectRole={props.i18nSelectRole}
            i18nRemoveRoleRow={props.i18nRemoveRoleRow}
            i18nRoleExists={props.i18nRoleExists}
            viewRolePermissionList={props.viewRolePermissionList}
            roles={props.dvRoles}
            clearAction={clearAction}
            selectedRoles={rolePermissionModel}
            updateRolePermissionModel={updateRolePermissionModel}
            deleteRoleFromPermissionModel={deleteRoleFromPermissionModel}
          />
          <Split gutter="sm" style={{ paddingTop: '15px' }}>
            <SplitItem>
              <ButtonLink
                key="confirm"
                onClick={handleUpdateRoles}
                as={'primary'}
                disabled={!saveEnabled}
              >
                {showLoading ? <Loader size={'xs'} inline={true} /> : null}
                {props.i18nSave}
              </ButtonLink>
            </SplitItem>
            <SplitItem>
              <Button variant="link" onClick={handleCancel}>
                {props.i18nCancel}
              </Button>
            </SplitItem>
          </Split>
        </>
      </DataListContent>
    </DataListItem>
  );
};
