import { Button } from '@patternfly/react-core';
import { RetweetIcon } from '@patternfly/react-icons';
import { EmptyState } from 'patternfly-react';
import * as React from 'react';
import { IListViewToolbarProps, ListViewToolbar } from '../../../Shared';
import './ViewInfoList.css';

export interface IViewInfoListProps extends IListViewToolbarProps {
  i18nEmptyStateInfo: string;
  i18nEmptyStateTitle: string;
  i18nName: string;
  i18nNameFilterPlaceholder: string;
  i18nRefresh: string;
  i18nLoading: string;
  loading: boolean;
  refreshSchemaConnections: () => void;
}

export const ViewInfoList: React.FunctionComponent<IViewInfoListProps> = props => {
  const handleRefreshClick = () => {
    props.refreshSchemaConnections();
  };

  return (
    <>
      <ListViewToolbar {...props}>
        <div className="form-group">
          <Button variant="secondary" onClick={handleRefreshClick}>
            {props.loading ? props.i18nLoading : props.i18nRefresh}
            {!props.loading && <RetweetIcon key="icon" />}
          </Button>
        </div>
      </ListViewToolbar>
      {props.children ? (
        <div className={'view-info-list'}>{props.children}</div>
      ) : (
        <EmptyState>
          <EmptyState.Icon />
          <EmptyState.Title>{props.i18nEmptyStateTitle}</EmptyState.Title>
          <EmptyState.Info>{props.i18nEmptyStateInfo}</EmptyState.Info>
        </EmptyState>
      )}
    </>
  );
};
