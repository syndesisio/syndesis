import { IRow, Table, TableBody, TableVariant } from '@patternfly/react-table';
import * as React from 'react';
import { FinalTreeChildComponent } from '..';

export interface IFirstTreeChildComponentProps {
  metadataTreeTables: Map<string, any>;
}

const getTableRows = (metadataTree: Map<string, any>) => {
  const tableRows: IRow[] = [];
  let index = 0;
  metadataTree.forEach((value, key) => {
    const theValue = {
      cells: [key],
      isOpen: false,
    } as IRow;
    tableRows.push(theValue);
    const childOne = {
      cells: [
        {
          title: <FinalTreeChildComponent metadataTreeColumns={value} />,
        },
      ],
      fullWidth: true,
      parent: index,
    };
    tableRows.push(childOne);
    index = index + 2;
  });

  return tableRows.length > 0 ? tableRows : [];
};

export const FirstTreeChildComponent: React.FunctionComponent<IFirstTreeChildComponentProps> = props => {
  const columns = [''];

  const [rowsList, setRowsList] = React.useState<IRow[]>(
    getTableRows(props.metadataTreeTables)
  );

  React.useEffect(() => {
    if (rowsList.length === 0) {
      setRowsList(getTableRows(props.metadataTreeTables));
    }
  }, [props.metadataTreeTables]);

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

  const onCollapse = (event: any, rowKey: any, isOpen: any) => {
    let rows;
    rows = [...rowsList];
    rows[rowKey].isOpen = isOpen;
    setRowsList(rows);
  };

  return (
    <Table
      aria-label="List of Tables in selected connection."
      onSelect={onSelect}
      variant={TableVariant.compact}
      onCollapse={onCollapse}
      cells={columns}
      rows={rowsList}
    >
      <TableBody />
    </Table>
  );
};
