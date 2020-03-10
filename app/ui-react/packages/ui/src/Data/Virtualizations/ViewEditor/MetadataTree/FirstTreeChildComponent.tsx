import { TableIcon } from '@patternfly/react-icons';
import { IRow, Table, TableBody, TableVariant } from '@patternfly/react-table';
import * as React from 'react';
import { FinalTreeChildComponent, KababActionComponent } from '..';

export interface IFirstTreeChildComponentProps {
  metadataTreeTables: Map<string, any>;
}

const getTableRows = (metadataTree: Map<string, any>) => {
  const tableRows: Array<IRow | string[]> = [];
  let index = 0;
  metadataTree.forEach((value, key) => {
    const theValue = {
      cells: [
        {
          title: (
            <div>
              <TableIcon />
              <span style={{ paddingLeft: '10px' }}>{key}</span>
              <KababActionComponent textData={key} />
            </div>
          ),
        },
      ],
      isOpen: false,
    } as IRow;
    tableRows.push(theValue);
    const childOne = {
      cells: [
        {
          title: <FinalTreeChildComponent metadataTreeColumns={value} />,
        },
      ],
      // fullWidth: true,
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

  const onCollapse = (event: any, rowKey: any, isOpen: any) => {
    let rows;
    rows = [...rowsList];
    rows[rowKey].isOpen = isOpen;
    setRowsList(rows);
  };

  return (
    <Table
      aria-label="List of Tables in selected connection."
      variant={TableVariant.compact}
      onCollapse={onCollapse}
      cells={columns}
      rows={rowsList}
    >
      <TableBody />
    </Table>
  );
};
