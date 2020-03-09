import { Spinner } from '@patternfly/react-core';
import { IRow, Table, TableBody, TableVariant } from '@patternfly/react-table';
import * as React from 'react';
import { FirstTreeChildComponent, KababActionComponent } from '..';

export interface IConnectionTreeComponentProps {
  metadataTree: Map<string, any>;
  i18nLoading: string;
}

const getTableTree = (sourceInfo: any): Map<string, any> => {
  const treeInfo = new Map<string, any>();

  for (const table of sourceInfo) {
    treeInfo.set(table.name, table.columns);
  }
  return treeInfo;
};

const getTableRows = (metadataTree: Map<string, any>) => {
  const tableRows: IRow[] = [];
  let index = 0;
  metadataTree.forEach((value, key) => {
    const theValue = {
      cells: [
        {
          title: (
            <div>
              <span>{key}</span>
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
          title: (
            <FirstTreeChildComponent metadataTreeTables={getTableTree(value)} />
          ),
        },
      ],
      parent: index,
    };
    tableRows.push(childOne);
    index = index + 2;
  });

  return tableRows.length > 0 ? tableRows : [];
};

export const ConnectionTreeComponent: React.FunctionComponent<IConnectionTreeComponentProps> = props => {
  const columns = [''];

  const [rowsList, setRowsList] = React.useState<IRow[]>(
    getTableRows(props.metadataTree)
  );

  React.useEffect(() => {
    if (rowsList.length === 0) {
      setRowsList(getTableRows(props.metadataTree));
    }
  }, [props.metadataTree]);

  const onCollapse = (event: any, rowKey: any, isOpen: any) => {
    let rows;
    rows = [...rowsList];
    rows[rowKey].isOpen = isOpen;
    setRowsList(rows);
  };

  return (
    // <Table
    //   aria-label="List of Tables in selected connection."
    //   variant={TableVariant.compact}
    //   onCollapse={onCollapse}
    //   cells={columns}
    //   rows={rowsList}
    // >
    //   <TableBody />
    // </Table>
    <>
    {rowsList.length === 0 ? (
      <>
      <Spinner size={'lg'} />
      {props.i18nLoading}
    </>
    ): (
      <Table
      aria-label="List of Tables in selected connection."
      variant={TableVariant.compact}
      onCollapse={onCollapse}
      cells={columns}
      rows={rowsList}
    >
      <TableBody />
    </Table>
    )}
    </>
  );
};
