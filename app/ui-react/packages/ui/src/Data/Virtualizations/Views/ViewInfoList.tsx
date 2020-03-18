import { 
  Button,
  ButtonVariant,
  EmptyState, 
  EmptyStateBody,
  EmptyStateVariant,
  Flex,
  FlexItem,
  FlexModifiers,
  Title } from '@patternfly/react-core';
import { SyncIcon } from '@patternfly/react-icons';
import * as React from 'react';
import { IListViewToolbarProps, ListViewToolbar } from '../../../Shared';
import './ViewInfoList.css';

export interface IViewInfoListProps extends IListViewToolbarProps {
  connectionLoading: boolean;
  connectionName: string;
  connectionStatus: JSX.Element;
  i18nEmptyStateInfo: string;
  i18nEmptyStateTitle: string;
  i18nLastUpdatedMessage: string;
  i18nName: string;
  i18nNameFilterPlaceholder: string;
  i18nRefresh: string;
  refreshConnectionSchema: (connectionName: string) => void;
}

export const ViewInfoList: React.FunctionComponent<
  IViewInfoListProps
> = props => {
  
  const handleRefreshClick = () => {
    props.refreshConnectionSchema(props.connectionName);
  };

  return (
    <>
      <ListViewToolbar {...props}>
        <div className="form-group">
          <Flex breakpointMods={[{modifier: FlexModifiers["space-items-xl"]}]}>
            <Flex>
              <FlexItem>
                <div>
                  <b>{props.connectionName}</b>
                </div>
              </FlexItem>
              <FlexItem>
                {props.connectionStatus}
              </FlexItem>
            </Flex>
            <Flex>
              <FlexItem>
                {props.i18nLastUpdatedMessage}
              </FlexItem>
              <FlexItem>
                <Button
                  data-testid={'view-info-list__refresh-button'}
                  variant={ButtonVariant.secondary}
                  onClick={handleRefreshClick}
                  isDisabled={props.connectionLoading}
                >
                  {<SyncIcon />}&nbsp;
                  {props.i18nRefresh}
                </Button>
               </FlexItem>
            </Flex>
          </Flex>
        </div>
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
