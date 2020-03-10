import { OutlinedCopyIcon } from '@patternfly/react-icons';
import { IRow, Table, TableBody, TableVariant } from '@patternfly/react-table';
import * as React from 'react';
import './FinalTreeChildComponent.css';

export interface ISourceColumn {
  name: string;
  datatype: string;
}

export interface IFinalTreeChildComponentProps {
  metadataTreeColumns: ISourceColumn[];
}

export const FinalTreeChildComponent: React.FunctionComponent<IFinalTreeChildComponentProps> = props => {
  const columns = [''];

  const getTableRows = (metadataTree: ISourceColumn[]) => {
    const tableRows: Array<IRow | string[]> = [];
    for (const column of metadataTree) {
      const theValue = {
        cells: [
          {
            title: (
              <div className={'final-tree-child-component_kabab'}>
                <span>{column.name}</span>
                <OutlinedCopyIcon
                  className={'final-tree-child-component_kabab_copy'}
                />
              </div>
            ),
          },
        ],
        // fullWidth: true,
      } as IRow;
      tableRows.push(theValue);
    }

    return tableRows.length > 0 ? tableRows : [];
  };

  const [rowsList, setRowsList] = React.useState<IRow[]>(
    getTableRows(props.metadataTreeColumns)
  );

  React.useEffect(() => {
    if (rowsList.length === 0) {
      setRowsList(getTableRows(props.metadataTreeColumns));
    }
  }, [props.metadataTreeColumns]);

  return (
    <Table
      aria-label="List of Tables in selected connection."
      variant={TableVariant.compact}
      cells={columns}
      rows={rowsList}
    >
      <TableBody />
    </Table>
  );
};
