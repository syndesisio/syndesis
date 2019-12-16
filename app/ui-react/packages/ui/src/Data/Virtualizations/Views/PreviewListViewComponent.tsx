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

export interface IPreviewListViewComponentProps {
  toggle: (id: string) => void;
  expanded: string[];
  name: string;
  index: number;
}

export const PreviewListViewComponent: React.FunctionComponent<IPreviewListViewComponentProps> = props => {
  const columns = ['', ''];
  const rows = [
    ['one', 'two'],
    ['three', 'four'],
    ['one', 'two'],
  ];

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
                  <Text component={TextVariants.h4}>{props.name}</Text>
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
                <DropdownItem key="link">Preview data</DropdownItem>,
                <DropdownItem key="action" component="button">
                  Remove Selection
                </DropdownItem>,
              ]}
            />
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
