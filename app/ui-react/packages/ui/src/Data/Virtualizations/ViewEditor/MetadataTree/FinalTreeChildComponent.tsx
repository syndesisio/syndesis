import { IRow, Table, TableBody, TableVariant } from '@patternfly/react-table';
import * as React from 'react';

export interface ISourceColumn {
  name: string;
  datatype: string;
}

export interface IFinalTreeChildComponentProps {
  metadataTreeColumns: ISourceColumn[];
}

const getTableRows = (metadataTree: ISourceColumn[]) => {
  const tableRows: IRow[] = [];
  for (const column of metadataTree) {
    const theValue = {
      cells: [column.name],
      fullWidth: true,
    } as IRow;
    tableRows.push(theValue);
  }

  return tableRows.length > 0 ? tableRows : [];
};

export const FinalTreeChildComponent: React.FunctionComponent<IFinalTreeChildComponentProps> = props => {
  const columns = [''];

  const [rowsList, setRowsList] = React.useState<IRow[]>(
    getTableRows(props.metadataTreeColumns)
  );

  React.useEffect(() => {
    if (rowsList.length === 0) {
      setRowsList(getTableRows(props.metadataTreeColumns));
    }
  }, [props.metadataTreeColumns]);

  const onSelect = (event: any, isSelected: any, rowId: any) => {
    let rows;
    if (rowId === -1) {
      rows = rowsList.map(oneRow => {
        oneRow.selected = isSelected;
        return oneRow;
      });
    } else {
      rows = [...rowsList];
      rows[rowId].selected = isSelected;
    }
    setRowsList(rows);
  };

  return (
    <Table
      aria-label="List of Tables in selected connection."
      onSelect={onSelect}
      variant={TableVariant.compact}
      cells={columns}
      rows={rowsList}
    >
      <TableBody />
    </Table>
  );
};
