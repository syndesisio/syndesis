import { TableIcon } from '@patternfly/react-icons';
import { IRow, Table, TableBody, TableVariant } from '@patternfly/react-table';
import * as React from 'react';
import { KababActionComponent, TreeColumnNodeComponent } from '..';

export interface ITreeTableNodeComponentProps {
  metadataTreeTables: Map<string, any>;
  i18nKababAction: string;
  i18nColumnActionTooltip: string;
  copyToDdlEditor: (insertText: string) => void;
}

export const TreeTableNodeComponent: React.FunctionComponent<ITreeTableNodeComponentProps> = React.memo(
  props => {
    const columns = [''];

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
                  <KababActionComponent textData={key} i18nKababAction={props.i18nKababAction}
                  copyToDdlEditor={props.copyToDdlEditor} />
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
              title: <TreeColumnNodeComponent metadataTreeColumns={value} i18nColumnActionTooltip={props.i18nColumnActionTooltip} copyToDdlEditor={props.copyToDdlEditor}/>,
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

    const memoisedValue = React.useMemo(
      () => getTableRows(props.metadataTreeTables),
      [props.metadataTreeTables]
    );

    const [rowsList, setRowsList] = React.useState<IRow[]>(memoisedValue);

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
  }
);
