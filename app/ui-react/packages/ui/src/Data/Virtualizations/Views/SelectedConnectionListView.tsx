import {
  DataList,
  DataListAction,
  DataListCell,
  DataListContent,
  DataListItem,
  DataListItemCells,
  DataListItemRow,
  DataListToggle,
  Dropdown,
  DropdownItem,
  DropdownPosition,
  KebabToggle,
  Text,
  TextContent,
  TextVariants,
} from '@patternfly/react-core';
import { Table, TableBody, TableVariant } from '@patternfly/react-table';
import * as React from 'react';
import './SelectedConnectionListView.css';

export interface ISelectedConnectionListViewProps {
  expanded: string[];
  name: string;
  connectionIcon: React.ReactNode;
  connectionName: string;
  index: number;
  rows: string[][];
  i18nPreviewData: string;
  i18nRemoveSelection: string;
  toggle: (id: string) => void;
  onTableRemoved: (connectionName: string, teiidName: string) => void;
  setShowPreviewData: (connectionName: string, tableName: string) => void;
}

export const SelectedConnectionListView: React.FunctionComponent<ISelectedConnectionListViewProps> = props => {
  const columns = ['', ''];

  /* States used in component */
  const [isOpen, setIsOpen] = React.useState(false);

  /* DataList kabab menu option handler */
  const onTableRemovedHandler = () => {
    props.onTableRemoved(props.connectionName, props.name);
  };

  const onPreviewClickHandler = () => {
    props.setShowPreviewData(props.connectionName, props.name);
  };

  /* DataList Dropdown events */ 
  const onSelect = (event?: Event) => {
    setIsOpen(!isOpen);
  };

  // tslint:disable-next-line: no-shadowed-variable
  const onToggle = (isOpen: boolean) => {
    setIsOpen(isOpen);
  };

  return (
    <DataList aria-label="Expandable data list example">
      <DataListItem
        aria-labelledby={`selected-table${props.index}`}
        isExpanded={props.expanded.includes(`ex-toggle${props.index}`)}
      >
        <DataListItemRow>
          <DataListToggle
            // tslint:disable-next-line: jsx-no-lambda
            onClick={() => props.toggle(`ex-toggle${props.index}`)}
            isExpanded={false}
            id={`ex-toggle${props.index}`}
            aria-controls={`selected-table-expand${props.index}`}
          />
          <DataListItemCells
            dataListCells={[
              <DataListCell key="primary content">
                <TextContent>
                  <Text component={TextVariants.h4}>
                    <span
                      className={'selected_connection_list_view__tableName'}
                    >
                      {props.name}
                    </span>
                    (&nbsp;{props.connectionIcon}
                    &nbsp;<span>{props.connectionName}&nbsp;)</span>
                  </Text>
                </TextContent>
              </DataListCell>,
            ]}
          />
          <DataListAction
            aria-labelledby={`selected-table${props.index} selected-table-action${props.index}`}
            id={`selected-table-action${props.index}`}
            aria-label="Actions"
          >
            <Dropdown
              isPlain={true}
              position={DropdownPosition.right}
              isOpen={isOpen}
              // tslint:disable-next-line: jsx-no-lambda
              onSelect={() => onSelect(event)}
              toggle={<KebabToggle onToggle={onToggle} />}
              dropdownItems={[
                <DropdownItem key="link" onClick={onPreviewClickHandler}>
                  {props.i18nPreviewData}
                </DropdownItem>,
                <DropdownItem
                  key="action"
                  component="button"
                  onClick={onTableRemovedHandler}
                >
                  {props.i18nRemoveSelection}
                </DropdownItem>,
              ]}
            />
          </DataListAction>
        </DataListItemRow>
        <DataListContent
          aria-label="Selected table content"
          id={`selected-table-expand${props.index}`}
          isHidden={!props.expanded.includes(`ex-toggle${props.index}`)}
        >
          <Table
            aria-label="Table column and its data type"
            variant={TableVariant.compact}
            borders={false}
            cells={columns}
            rows={props.rows}
          >
            <TableBody />
          </Table>
        </DataListContent>
      </DataListItem>
    </DataList>
  );
};
