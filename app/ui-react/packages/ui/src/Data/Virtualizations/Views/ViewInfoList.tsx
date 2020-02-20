import { EmptyState, EmptyStateBody, EmptyStateVariant, Title } from '@patternfly/react-core';
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
        <div className={'view-info-list'}>{props.children}</div>
      ) : (
        <EmptyState variant={EmptyStateVariant.full}>
          <Title headingLevel="h5" size="lg">
            {props.i18nEmptyStateTitle}
          </Title>
          <EmptyStateBody>{props.i18nEmptyStateInfo}</EmptyStateBody>
        </EmptyState>
      )}
    </>
  );
};
