import { Spinner } from '@patternfly/react-core';
import { DatabaseIcon } from '@patternfly/react-icons';
import { IRow, Table, TableBody, TableVariant } from '@patternfly/react-table';
import * as React from 'react';
import { TreeTableNodeComponent } from '..';

export interface IConnectionTreeComponentProps {
  metadataTree: Map<string, any>;
  i18nLoading: string;
  i18nKababAction: string;
  i18nColumnActionTooltip: string;
  copyToDdlEditor: (insertText: string) => void;
}

const compPropsAreEqual = (prevProps: any, nextProps: any) => {
  return prevProps.metadataTree === nextProps.metadataTree;
};
export const ConnectionTreeComponent: React.FunctionComponent<IConnectionTreeComponentProps> = React.memo(
  props => {
    const columns = [''];

    const getTableTree = (sourceInfo: any): Map<string, any> => {
      const treeInfo = new Map<string, any>();

      for (const table of sourceInfo) {
        treeInfo.set(table.name, table.columns);
      }
      return treeInfo;
    };

    const getTableRows = (metadataTree: Map<string, any>) => {
      const tableRows: Array<IRow | string[]> = [];
      let index = 0;
      metadataTree.forEach((value, key) => {
        const theValue = {
          cells: [
            {
              title: (
                <div>
                  <DatabaseIcon />
                  <span style={{ paddingLeft: '10px' }}>{key}</span>
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
                <TreeTableNodeComponent
                  metadataTreeTables={getTableTree(value)}
                  i18nKababAction={props.i18nKababAction}
                  i18nColumnActionTooltip={props.i18nColumnActionTooltip}
                  copyToDdlEditor={props.copyToDdlEditor}
                />
              ),
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
    const memoisedValue = React.useMemo(
      () => getTableRows(props.metadataTree),
      [props.metadataTree]
    );

    const [rowsList, setRowsList] = React.useState<IRow[]>(memoisedValue);

    React.useEffect(() => {
      setRowsList(memoisedValue);
    }, [props.metadataTree]);

    const onCollapse = (event: any, rowKey: any, isOpen: any) => {
      let rows;
      rows = [...rowsList];
      rows[rowKey].isOpen = isOpen;
      setRowsList(rows);
    };

    return (
      <>
        {rowsList.length === 0 ? (
          <>
            <Spinner size={'lg'} />
            {props.i18nLoading}
          </>
        ) : (
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
  },
  compPropsAreEqual
);
