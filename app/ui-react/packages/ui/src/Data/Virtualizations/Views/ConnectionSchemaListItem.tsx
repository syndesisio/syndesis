import {
  Tooltip
} from '@patternfly/react-core';
import { Label, ListView, ListViewItem, Spinner } from 'patternfly-react';
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
            {props.loading && props.dvStatus !== ConnectionStatus.ACTIVE ? (
              <Spinner loading={true} inline={true} />
            ) : (
              <></>
            )}
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
        initExpanded={props.haveSelectedSource}
        stacked={false}
      >
        {props.children ? <ListView>{props.children}</ListView> : null}
      </ListViewItem>
    </>
  );
};
