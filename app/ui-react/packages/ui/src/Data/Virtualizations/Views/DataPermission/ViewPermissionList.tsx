import {
  Alert,
  AlertVariant,
  Button,
  DataList,
  DataListCell,
  DataListCheck,
  DataListItem,
  DataListItemCells,
  DataListItemRow,
  DataListToggle,
  Modal,
  Radio,
  Split,
  SplitItem,
  Text,
  TextVariants,
} from '@patternfly/react-core';
import * as H from '@syndesis/history';
import * as React from 'react';
import { EmptyViewsState, RolePermissionList } from '..';
import { PageSection } from '../../../../Layout';
import { IListViewToolbarProps } from '../../../../Shared';
import './ViewPermissionList.css';
import { ViewPermissionToolbar } from './ViewPermissionToolbar';

export interface IViewPermissionList extends IListViewToolbarProps {
  hasListData: boolean;
  i18nViewName: string;
  i18nPermission: string;

  i18nEmptyStateInfo: string;
  i18nEmptyStateTitle: string;
  i18nImportViews: string;
  i18nImportViewsTip: string;
  linkCreateViewHRef: H.LocationDescriptor;
  linkImportViewsHRef: H.LocationDescriptor;
  i18nCreateViewTip?: string;
  i18nCreateView: string;

  page: number;
  perPage: number;
  setPage: (page: number) => void;
  setPerPage: (perPage: number) => void;
  clearViewSelection: () => void;
  selectAllViews: () => void;
  selectPageViews: () => void;
  hasViewSelected: boolean;
  i18nSelectNone: string;
  i18nSelectPage: string;
  i18nSelectAll: string;
  i18nCancle: string;
  i18nClearPermission: string;
  i18nSave: string;
  i18nAddNewRole: string;
  i18nSelect: string;
  i18nInsert: string;
  i18nUpdate: string;
  i18nDelete: string;
  i18nAllAccess: string;
  i18nRole: string;
  i18nSelectedViewsMsg: string;
  i18nSetPermission: string;
  i18nSsoConfigWarning: string;
  status: any;
  dvRoles: string[];
  itemSelected: Map<string, string>;
  updateViewsPermissions: (roleInfo: IRoleInfo) => Promise<boolean>;
  getDVStatusUpdate: () => void;
  getUpdatedRole: () => void;
}

export interface ITablePrivilege {
  grantPrivileges: string[];
  roleName: string | undefined;
  viewDefinitionIds: string[];
}

export interface IRoleInfo {
  operation: 'GRANT' | 'REVOKE';
  tablePrivileges: ITablePrivilege[];
}

const getUpdatePermissionsPayload = (
  permissionsModel: Map<string, string[]>,
  itemSelected: Map<string, string>
) => {
  const returnVal: ITablePrivilege[] = [];
  permissionsModel.forEach((value: string[], key: string) => {
    returnVal.push({
      grantPrivileges: value,
      roleName: key,
      viewDefinitionIds: Array.from(itemSelected.keys()),
    });
  });

  return returnVal;
};

export const ViewPermissionList: React.FunctionComponent<IViewPermissionList> = props => {
  /**
   * React useState Hook to handle state in component.
   */
  const [isSetModalOpen, setIsSetModalOpen] = React.useState<boolean>(false);

  const [isClearModalOpen, setIsClearModalOpen] = React.useState<boolean>(
    false
  );

  const [rolePermissionModel, setRolePermissionModel] = React.useState<
    Map<string, string[]>
  >(new Map());

  const [showMore, setShowMore] = React.useState<boolean>(false);

  const [grantOperation, setGrantOperation] = React.useState<boolean>(true);

  let selectedViewText = Array.from(props.itemSelected.values()).join(', ');

  const updateRolePermissionModel = (
    roleName: string,
    permissions: string[]
  ) => {
    const rolePermissionModelCopy = new Map<string, string[]>(
      rolePermissionModel
    );
    setRolePermissionModel(rolePermissionModelCopy.set(roleName, permissions));
  };

  const clearRolePermissionModel = () => {
    setRolePermissionModel(new Map<string, string[]>());
  };

  const handleClearRoles = async () => {
    const clearPayload = {
      grantPrivileges: ['SELECT', 'INSERT', 'UPDATE', 'DELETE'],
      roleName: undefined,
      viewDefinitionIds: Array.from(props.itemSelected.keys()),
    };
    const callSucess = await props.updateViewsPermissions({
      operation: 'REVOKE',
      tablePrivileges: [clearPayload],
    });
    if (callSucess) {
      props.clearViewSelection();
      // toggle modal closed
      setIsClearModalOpen(!isClearModalOpen);
    }
  };

  const handleUpdateRoles = async () => {
    const callSucess = await props.updateViewsPermissions({
      operation: grantOperation ? 'GRANT' : 'REVOKE',
      tablePrivileges: getUpdatePermissionsPayload(
        rolePermissionModel,
        props.itemSelected
      ),
    });
    if (callSucess) {
      props.clearViewSelection();
      clearRolePermissionModel();
      setGrantOperation(true);
      // close setPermissionModel
      setIsSetModalOpen(false);
    }
  };

  const handleSetModalToggle = () => {
    props.getUpdatedRole();
    props.getDVStatusUpdate();
    setIsSetModalOpen(!isSetModalOpen);
  };

  const handleClearModalToggle = () => {
    setIsClearModalOpen(!isClearModalOpen);
  };

  React.useEffect(() => {
    selectedViewText = Array.from(props.itemSelected.values()).join(', ');
    if (selectedViewText.length > 200) {
      setShowMore(true);
    }
  }, [props.itemSelected]);

  return (
    <PageSection>
      {props.hasListData ? (
        <React.Fragment>
          <ViewPermissionToolbar
            {...props}
            handleSetModalToggle={handleSetModalToggle}
            handleClearModalToggle={handleClearModalToggle}
          />
          <Modal
            isLarge={true}
            title="Set permission"
            isOpen={isSetModalOpen}
            onClose={handleSetModalToggle}
            actions={[
              <Button
                key="confirm"
                variant="primary"
                isDisabled={rolePermissionModel.size < 1}
                onClick={handleUpdateRoles}
              >
                {props.i18nSave}
              </Button>,
              <Button
                key="cancel"
                variant="link"
                onClick={handleSetModalToggle}
              >
                {props.i18nCancle}
              </Button>,
            ]}
            isFooterLeftAligned={true}
          >
            <div className={'view-permission-list_set-model'}>
              <h3 className={'view-permission-list_model-text-size'}>
                {props.i18nSelectedViewsMsg}
                <b
                  className={
                    showMore ? 'view-permission-list_model-text-truncate' : ''
                  }
                >
                  <i>{selectedViewText}</i>
                </b>
                {selectedViewText.length > 200 && (
                  <Button
                    variant={'link'}
                    // tslint:disable-next-line: jsx-no-lambda
                    onClick={() => setShowMore(!showMore)}
                  >
                    {showMore ? 'show more' : 'show less'}
                  </Button>
                )}
              </h3>
            </div>
            <Split
              gutter="sm"
              className={'view-permission-list_set-model_grant'}
            >
              <SplitItem>
                <Radio
                  aria-label={'grant'}
                  id={'operation-grant'}
                  data-testid={'operation-grant'}
                  name={'operation'}
                  label="GRANT"
                  // tslint:disable-next-line: jsx-no-lambda
                  onClick={() => setGrantOperation(true)}
                  isChecked={grantOperation}
                />
              </SplitItem>
              <SplitItem>
                <Radio
                  aria-label={'revoke'}
                  id={'operation-revoke'}
                  className={'view-permission-list_radios'}
                  data-testid={'operation-revoke'}
                  name={'operation'}
                  label="REVOKE"
                  // tslint:disable-next-line: jsx-no-lambda
                  onClick={() => setGrantOperation(false)}
                  isChecked={!grantOperation}
                />
              </SplitItem>
            </Split>
            {props.status.ssoConfigured === 'false' && (
              <Alert
                variant={AlertVariant.warning}
                isInline={true}
                title={props.i18nSsoConfigWarning}
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
              viewRolePermissionList={[]}
              roles={props.dvRoles}
              updateRolePermissionModel={updateRolePermissionModel}
            />
          </Modal>
          <Modal
            width={'50%'}
            title={props.i18nClearPermission}
            isOpen={isClearModalOpen}
            onClose={handleClearModalToggle}
            actions={[
              <Button
                key="confirm"
                variant="primary"
                onClick={handleClearRoles}
              >
                {props.i18nClearPermission}
              </Button>,
              <Button
                key="cancel"
                variant="link"
                onClick={handleClearModalToggle}
              >
                {props.i18nCancle}
              </Button>,
            ]}
            isFooterLeftAligned={true}
          >
            {' '}
            <div className={'view-permission-list_clear-model'}>
              <h3 className={'view-permission-list_model-text-size'}>
                Continue with this action will clear all the existing
                permissions for selected views. are you sure you want to clear
                permission?
              </h3>
            </div>
          </Modal>
          <DataList aria-label={'views list'}>
            <DataListItem
              aria-labelledby="view-permission-heading"
              isExpanded={false}
            >
              <DataListItemRow>
                <DataListToggle
                  isExpanded={false}
                  id="view-permission-heading-toggle"
                  aria-controls="view-permission-heading-expand"
                  className={'view-permission-list-list_heading'}
                />
                <DataListCheck
                  aria-labelledby="view-permission-heading-check"
                  checked={false}
                  className={'view-permission-list-list_heading'}
                />
                <DataListItemCells
                  dataListCells={[
                    <DataListCell width={1} key="view">
                      <Text
                        component={TextVariants.h3}
                        className={'view-permission-list_headingText'}
                      >
                        {props.i18nViewName}
                      </Text>
                    </DataListCell>,
                    <DataListCell width={5} key="permission">
                      <Text
                        component={TextVariants.h2}
                        className={'view-permission-list_headingText'}
                      >
                        {props.i18nPermission}
                      </Text>
                    </DataListCell>,
                  ]}
                />
              </DataListItemRow>
            </DataListItem>
            {props.children}
          </DataList>
        </React.Fragment>
      ) : (
        <EmptyViewsState
          i18nEmptyStateTitle={props.i18nEmptyStateTitle}
          i18nEmptyStateInfo={props.i18nEmptyStateInfo}
          i18nCreateView={props.i18nCreateView}
          i18nCreateViewTip={props.i18nCreateViewTip}
          i18nImportViews={props.i18nImportViews}
          i18nImportViewsTip={props.i18nImportViewsTip}
          linkCreateViewHRef={props.linkCreateViewHRef}
          linkImportViewsHRef={props.linkImportViewsHRef}
        />
      )}
    </PageSection>
  );
};
