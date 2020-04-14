import {
  DataList,
  DataListAction,
  DataListCell,
  DataListContent,
  DataListItem,
  DataListItemCells,
  DataListItemRow,
  DataListToggle,
  Label, 
  Split, 
  SplitItem, 
  Title, 
  Tooltip,
} from '@patternfly/react-core';
import { global_active_color_100, global_danger_color_100 } from '@patternfly/react-tokens';
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
        {(props.children && React.Children.toArray(props.children).length > 0) &&
        <DataListToggle
          isExpanded={isExpanded}
          id="connection-schema-list-item-expand"
          aria-controls="connection-schema-list-item-expand"
          onClick={() => setExpanded(!isExpanded)}
        />
        }
        <DataListItemCells
          dataListCells={[
            <DataListCell width={1} key={0}>
              <span>
                {props.icon}
                <Tooltip content={props.dvStatusTooltip} position={'bottom'}>
                  <Label
                    className="connection-schema-list-item__status"
                    style={
                      props.dvStatus === ConnectionStatus.ACTIVE
                        ? { background: global_active_color_100.value }
                        : { background: global_danger_color_100.value }
                    }
                  >
                    {props.dvStatus}
                  </Label>
                </Tooltip>
              </span>
            </DataListCell>,
            <DataListCell key={'primary content'} width={4}>
              <div className={'connection-schema-list-item__text-wrapper'}>
                <b>{props.connectionName}</b>
                <br />
                {props.connectionDescription ? props.connectionDescription : ''}
              </div>
            </DataListCell>,
          ]}
        />
        <DataListAction
          aria-labelledby={'connection schema list actions'}
          id={'connection-schema-list-actions'}
          aria-label={'Actions'}
        >
          <Split>
            <SplitItem>
              {props.loading ? (
                <Title size="md">{props.i18nRefreshInProgress}</Title>
              ) : (
                <></>
              )}
            </SplitItem>
          </Split>
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
