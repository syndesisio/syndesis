import {
  Split, SplitItem, Title, Tooltip
} from '@patternfly/react-core';
import { Label, ListView, ListViewItem } from 'patternfly-react';
import * as React from 'react';
import { toValidHtmlId } from '../../../helpers';
import { ConnectionStatus } from '../../DvConnection/DvConnectionCard';
import './ConnectionSchemaListItem.css';

export interface IConnectionSchemaListItemProps {
  connectionName: string;
  connectionDescription: string;
  dvStatusTooltip: string;
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
            <Tooltip content={props.dvStatusTooltip} position={'bottom'}>
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
            </Tooltip>
          </span>
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
