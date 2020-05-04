import {
  Button,
  DataList,
  DataListCell,
  DataListCheck,
  DataListItem,
  DataListItemCells,
  DataListItemRow,
  DataListToggle,
  Modal,
  Text,
  TextVariants,
} from '@patternfly/react-core';
import * as React from 'react';
import { RolePermissionList } from '..';
import { PageSection } from '../../../../Layout';
import { IListViewToolbarProps } from '../../../../Shared';
import './ViewPermissionList.css';
import { ViewPermissionToolbar } from './ViewPermissionToolbar';
export interface IViewPermissionList extends IListViewToolbarProps {
  hasListData: boolean;
  i18nViewName: string;
  i18nPermission: string;

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
  i18nRead: string;
  i18nEdit: string;
  i18nDelete: string;
  i18nAllAccess: string;
  i18nRole: string;
  i18nSelectedViews: string;
  i18nSelectedViewsMsg: string;
  i18nSetPermission: string;
}

export const ViewPermissionList: React.FunctionComponent<IViewPermissionList> = props => {
  /**
   * React useState Hook to handle state in component.
   */
  const [isSetModalOpen, setIsSetModalOpen] = React.useState<boolean>(false);

  const [isClearModalOpen, setIsClearModalOpen] = React.useState<boolean>(
    false
  );

  const [showMore, setShowMore] = React.useState<boolean>(false);

  const handleSetModalToggle = () => {
    // setRoleRowList([]);
    setIsSetModalOpen(!isSetModalOpen);
  };

  const handleClearModalToggle = () => {
    setIsClearModalOpen(!isClearModalOpen);
  };

  React.useEffect(() => {
    if (props.i18nSelectedViews.length > 200) {
      setShowMore(true);
    }
  }, [props.i18nSelectedViews]);

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
                onClick={handleSetModalToggle}
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
                  <i>{props.i18nSelectedViews}</i>
                </b>
                {props.i18nSelectedViews.length > 200 && (
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
            <RolePermissionList
              i18nRole={props.i18nRole}
              i18nRead={props.i18nRead}
              i18nEdit={props.i18nEdit}
              i18nDelete={props.i18nDelete}
              i18nAllAccess={props.i18nAllAccess}
              i18nAddNewRole={props.i18nAddNewRole}
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
                onClick={handleClearModalToggle}
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
        <div>
          <p>Empty</p>
        </div>
      )}
    </PageSection>
  );
};
