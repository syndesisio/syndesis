import * as React from 'react';

import { Label } from '@patternfly/react-core';
import {
  headerCol,
  IRow,
  Table,
  TableBody,
  TableHeader,
} from '@patternfly/react-table';

export interface IViewInfoListItemsProps {
  filteredAndSorted: any[];
  selectedViewNames: string[];
  onSelectionChanged: (name: string, selected: boolean) => void;
  handleSelectAll: (isSelected: boolean, AllViewInfo: any[]) => void;
}

export const ViewInfoListItems: React.FunctionComponent<IViewInfoListItemsProps> = (props) => {

  const [columns, setColumns] = React.useState([
    { title: `Select All (${props.selectedViewNames.length} of ${props.filteredAndSorted.length} items)`, cellTransforms: [headerCol()] },
    '',
    ''
  ]);

  const getTableRows = () => {
    const sizedRows: IRow[] = [];
    for (const row of props.filteredAndSorted) {
      const rowValues: IRow = [];
        const theValue = {
          title: row.viewName,
        } as IRow;
        rowValues.push(theValue);
        const nodePath = row.nodePath.join('/');
        rowValues.push(nodePath)
        if(row.isUpdate){
          rowValues.push(<Label type="warning">Update</Label>)
        }else{
          rowValues.push(<span/>);
        }
        if(props.selectedViewNames.includes(row.viewName)){
          rowValues.selected = true;
        }
      sizedRows.push(rowValues);
    }
    return sizedRows.length > 0 ? sizedRows : [];
  };

  let rowsList = getTableRows(); 
  
  const [rowUpdate,setRowUpdate] = React.useState(false);

  React.useEffect(()=>{
    rowsList = getTableRows();
    const updatedColumn = [
      { title: `Select All (${props.selectedViewNames.length} of ${props.filteredAndSorted.length} items)`, cellTransforms: [headerCol()] },
      '',
      ''
    ]
    setColumns(updatedColumn);
  },[rowUpdate]);
  

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
      cells={columns}
      rows={rowsList}
    >
      <TableHeader />
      <TableBody />
    </Table>
  );
};
