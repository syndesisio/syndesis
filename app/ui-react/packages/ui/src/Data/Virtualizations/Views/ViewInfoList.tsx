import { EmptyState, ListView } from 'patternfly-react';
import * as React from 'react';
import { IListViewToolbarProps, ListViewToolbar } from '../../../Shared';
import './ViewInfoList.css';

export interface IViewInfoListProps extends IListViewToolbarProps {
  i18nEmptyStateInfo: string;
  i18nEmptyStateTitle: string;
  i18nName: string;
  i18nNameFilterPlaceholder: string;
}

export const ViewInfoList: React.FunctionComponent<
  IViewInfoListProps
> = props => {
  return (
    <>
      <ListViewToolbar {...props}>
        <div />
      </ListViewToolbar>
      {props.children ? (
        <div className={'view-info-list'}>
          <ListView>{props.children}</ListView>
        </div>
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
