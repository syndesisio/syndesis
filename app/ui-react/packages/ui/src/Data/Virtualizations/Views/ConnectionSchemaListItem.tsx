import {
  Split,
  SplitItem,
  Title,
} from '@patternfly/react-core';
import { Label, ListView, ListViewInfoItem, ListViewItem } from 'patternfly-react';
import * as React from 'react';
import { toValidHtmlId } from '../../../helpers';
import { ConnectionStatus } from '../../DvConnection/DvConnectionCard';
import './ConnectionSchemaListItem.css';

export interface IConnectionSchemaListItemProps {
  connectionName: string;
  connectionDescription: string;
  dvStatusError: JSX.Element;
  dvStatus: string;
  haveSelectedSource: boolean;
  i18nRefreshInProgress: string;
  icon: React.ReactNode;
  loading: boolean;
}

export const ConnectionSchemaListItem: React.FunctionComponent<IConnectionSchemaListItemProps> = props => {
  return (
    <>
      <ListViewItem
        data-testid={`connection-schema-list-item-${toValidHtmlId(
          props.connectionName
        )}-list-item`}
        heading={props.connectionName}
        description={
          props.connectionDescription ? props.connectionDescription : ''
        }
        hideCloseIcon={true}
        leftContent={
          <span>
            {props.icon}
            <Label
              className="connection-schema-list-item__status"
              type={
                props.dvStatus === ConnectionStatus.ACTIVE
                  ? 'success'
                  : 'danger'
              }
            >
              {props.dvStatus}
            </Label>
          </span>
        }
        additionalInfo={
          props.dvStatus === ConnectionStatus.FAILED ? (
            [
              <ListViewInfoItem key={1}>
                {props.dvStatusError}
              </ListViewInfoItem>,
            ]
          ) : (
            <></>
          )
        }
        actions={
          <Split>
            <SplitItem>
              {props.loading ? (
                <Title size="md">{props.i18nRefreshInProgress}</Title>
              ) : (
                <></>
              )}
            </SplitItem>
          </Split>
        }
        initExpanded={props.haveSelectedSource}
        stacked={false}
      >
        {props.children && props.dvStatus === ConnectionStatus.ACTIVE ? (
          <ListView>{props.children}</ListView>
        ) : null}
      </ListViewItem>
    </>
  );
};
