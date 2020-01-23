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
import { DatabaseIcon } from '@patternfly/react-icons';
import { Table, TableBody, TableVariant } from '@patternfly/react-table';
import * as React from 'react';
import './SelectedConnectionListView.css'

export interface ISelectedConnectionListViewProps {
  toggle: (id: string) => void;
  expanded: string[];
  name: string;
  connectionName: string;
  index: number;
  rows: string[][];
  onTabelRemoved: (connectionName: string, teiidName: string) => void;
  setShowPreviewData: () => void;
}

export const SelectedConnectionListView: React.FunctionComponent<ISelectedConnectionListViewProps> = props => {
  const columns = ['', ''];

  const rows = props.rows;

  const onTrashClickHandler = () => {
    props.onTabelRemoved(props.connectionName, props.name);
  }

  const onPreviewClickHandlere = () =>{
    props.setShowPreviewData();
  }
  {/*   
  May use when implement the table preview
*/  }
  const [isOpen, setIsOpen] = React.useState(false);

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
                    <span className={'selected_connection_list_view__tableName'}>{props.name}</span>(<DatabaseIcon />&nbsp;<span>{props.connectionName})</span>
                  </Text>
                </TextContent>
              </DataListCell>
            ]}
          />
          <DataListAction
            aria-labelledby={`selected-table${props.index} selected-table-action${props.index}`}
            id={`selected-table-action${props.index}`}
            aria-label="Actions"
          >
            
            {/* May use when implement the table preview */}
            
            <Dropdown
              isPlain={true}
              position={DropdownPosition.right}
              isOpen={isOpen}
              // tslint:disable-next-line: jsx-no-lambda
              onSelect={() => onSelect(event)}
              toggle={<KebabToggle onToggle={onToggle} />}
              dropdownItems={[
                <DropdownItem key="link" onClick={onPreviewClickHandlere}>Preview data</DropdownItem>,
                <DropdownItem key="action" component="button" onClick={onTrashClickHandler}>
                  Remove Selection
                </DropdownItem>,
              ]}
            />
            {/* <Tooltip position={TooltipPosition.top} content={<div>Remove</div>}>
              <OutlinedTrashAltIcon onClick={onTrashClickHandler}/>
            </Tooltip> */}
          </DataListAction>
        </DataListItemRow>
        <DataListContent
          aria-label="Selected table contect"
          id={`selected-table-expand${props.index}`}
          isHidden={!props.expanded.includes(`ex-toggle${props.index}`)}
        >
          <Table
            aria-label="Table column and its data type"
            variant={TableVariant.compact}
            borders={false}
            cells={columns}
            rows={rows}
          >
            <TableBody />
          </Table>
        </DataListContent>
      </DataListItem>
    </DataList>
  );
};
