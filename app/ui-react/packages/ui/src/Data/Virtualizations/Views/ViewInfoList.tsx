import { EmptyState, ListView } from 'patternfly-react';
import * as React from 'react';
import { IListViewToolbarProps, ListViewToolbar } from '../../../Shared';

export interface IViewInfoListProps extends IListViewToolbarProps {
  i18nEmptyStateInfo: string;
  i18nEmptyStateTitle: string;
  i18nName: string;
  i18nNameFilterPlaceholder: string;
}

export class ViewInfoList extends React.Component<IViewInfoListProps> {
  public render() {
    return (
      <>
        <ListViewToolbar {...this.props}>
          <div />
        </ListViewToolbar>
        {this.props.children ? (
          <ListView>{this.props.children}</ListView>
        ) : (
          <EmptyState>
            <EmptyState.Icon />
            <EmptyState.Title>
              {this.props.i18nEmptyStateTitle}
            </EmptyState.Title>
            <EmptyState.Info>{this.props.i18nEmptyStateInfo}</EmptyState.Info>
          </EmptyState>
        )}
      </>
    );
  }
}
