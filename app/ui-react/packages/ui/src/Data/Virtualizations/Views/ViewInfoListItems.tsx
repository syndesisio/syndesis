import * as React from 'react';

import { Label } from '@patternfly/react-core';
import {
  headerCol,
  IRow,
  Table,
  TableBody,
  TableHeader,
  TableVariant,
} from '@patternfly/react-table';
import './ViewInfoListItems.css';

export interface IViewInfoListItemsProps {
  filteredAndSorted: any[];
  selectedViewNames: string[];
  onSelectionChanged: (name: string, selected: boolean) => void;
  handleSelectAll: (isSelected: boolean, AllViewInfo: any[]) => void;
  i18nUpdate: string;
  i18nSelectAll: string;
}

export const ViewInfoListItems: React.FunctionComponent<IViewInfoListItemsProps> = (props) => {

  const [columns, setColumns] = React.useState([
    { title: `${props.i18nSelectAll}`, cellTransforms: [headerCol()] },
    '',
    ''
  ]);

  const getTableRows = () => {
    const tableRows: IRow[] = [];
    for (const row of props.filteredAndSorted) {
      const rowValues: IRow = [];
      const theValue = {
        props: {
          className: 'view_info_list_items_tableName',
        },
        title: row.viewName,
      } as IRow;
      rowValues.push(theValue);
      const nodePath = `/${row.nodePath.join('/')}`;
      rowValues.push(nodePath);
    const updateLabel = row.isUpdate ? (<div><Label className={'view_info_list_items_labelColor'} isCompact={true}>{props.i18nUpdate}</Label></div>
        
      ) : (
        <span />
      );
      rowValues.push(updateLabel);
      if (props.selectedViewNames.includes(row.viewName)) {
        rowValues.selected = true;
      }
      tableRows.push(rowValues);
    }
    return tableRows.length > 0 ? tableRows : [];
  };

  let rowsList = getTableRows();

  const [rowUpdate, setRowUpdate] = React.useState(false);

  React.useEffect(()=>{
    rowsList = getTableRows();
    const updatedColumn = [
      { title: `${props.i18nSelectAll}`, cellTransforms: [headerCol()] },
      '',
      ''
    ]
    setColumns(updatedColumn);
  },[rowUpdate,props.i18nSelectAll]);
  
  const onSelect = (event: any, isSelected: any, rowId: any) => {
    let rows;
    if (rowId === -1) {
      rows = rowsList.map(oneRow => {
        oneRow.selected = isSelected;
        return oneRow;
      });
      props.handleSelectAll(isSelected, props.filteredAndSorted);
    } else {
      rows = [...rowsList];
      rows[rowId].selected = isSelected;
      props.onSelectionChanged(rows[rowId][0].title, isSelected);
    }
    setRowUpdate(!rowUpdate);
  };    

  return (
    <Table
      aria-label="List of Tables in selected connection."
      onSelect={onSelect}
      variant={TableVariant.compact}
      cells={columns}
      rows={rowsList}
    >
      <TableHeader />
      <TableBody />
    </Table>
  );
};
