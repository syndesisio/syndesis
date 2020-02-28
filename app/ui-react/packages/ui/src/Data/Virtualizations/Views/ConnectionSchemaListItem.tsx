import {
  DataList,
  DataListAction,
  DataListCell,
  DataListContent,
  DataListItem,
  DataListItemCells,
  DataListItemRow,
  DataListToggle,
  Split,
  SplitItem,
} from '@patternfly/react-core';
import * as React from 'react';
import { toValidHtmlId } from '../../../helpers';
import { ConnectionStatus } from '../../DvConnection/DvConnectionCard';
import { DvConnectionStatus } from '../../DvConnection/DvConnectionStatus';
import './ConnectionSchemaListItem.css';

export interface IConnectionSchemaListItemProps {
  connectionName: string;
  connectionDescription?: string;
  dvStatusMessage?: string;
  dvStatus: string;
  haveSelectedSource: boolean;
  i18nRefreshInProgress: string;
  i18nStatusErrorPopoverTitle: string;
  i18nStatusErrorPopoverLink: string;
  icon: React.ReactNode;
  loading: boolean;
}

export const ConnectionSchemaListItem: React.FunctionComponent<IConnectionSchemaListItemProps> = props => {

  const [isExpanded, setExpanded] = React.useState(props.haveSelectedSource);

  return (
    <DataListItem
      aria-labelledby={'connection schema list item'}
      data-testid={`connection-schema-list-item-${toValidHtmlId(
        props.connectionName
      )}-list-item`}
      className={'connection-schema-list-item'}
      isExpanded={isExpanded}
    >
      <DataListItemRow>
        {props.children &&
          React.Children.toArray(props.children).length > 0 && (
            <DataListToggle
              isExpanded={isExpanded}
              id="connection-schema-list-item-expand"
              aria-controls="connection-schema-list-item-expand"
              onClick={() => setExpanded(!isExpanded)}
            />
          )}
        <DataListItemCells
          dataListCells={[
            <DataListCell key={'primary content'} width={4}>
              <Split>
                <SplitItem>
                  <div className={'connection-schema-list-item__icon-wrapper'}>
                    {props.icon}
                  </div>
                </SplitItem>
                <SplitItem>
                  <div>
                    <b>{props.connectionName}</b>
                    <br />
                    {props.connectionDescription
                      ? props.connectionDescription
                      : ''}
                  </div>
                </SplitItem>
              </Split>
            </DataListCell>,
          ]}
        />
        <DataListAction
          aria-labelledby={'connection schema list actions'}
          id={'connection-schema-list-actions'}
          aria-label={'Actions'}
        >
          <DvConnectionStatus
            dvStatus={props.dvStatus}
            dvStatusMessage={props.dvStatusMessage}
            i18nRefreshInProgress={props.i18nRefreshInProgress}
            i18nStatusErrorPopoverTitle={props.i18nStatusErrorPopoverTitle}
            i18nStatusErrorPopoverLink={props.i18nStatusErrorPopoverLink}
            loading={props.loading}
          />
        </DataListAction>
      </DataListItemRow>
      {props.children && props.dvStatus === ConnectionStatus.ACTIVE ? (
        <DataListContent
          aria-label="Primary Content Details"
          id="connection-schema-content"
          isHidden={!isExpanded}
        >
          <DataList aria-label={'connection schema list content'}>
            {props.children}
          </DataList>
        </DataListContent>
      ) : null}
    </DataListItem>
  );
};
