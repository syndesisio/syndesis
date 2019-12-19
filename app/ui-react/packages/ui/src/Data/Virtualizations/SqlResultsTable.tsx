import {
  IRow,
  Table,
  TableBody,
  TableHeader,
  TableVariant,
} from '@patternfly/react-table';
import * as React from 'react';
import './SqlResultsTable.css';

export interface ISqlResultsTableProps {
  /**
   * Array of column info for the query results.  (The column id and display label)
   * Example:
   * [ { id: 'fName', label: 'First Name'},
   *   { id: 'lName', label: 'Last Name'},
   *   { id: 'country', label: 'Country' }
   * ]
   */
  queryResultCols: IColumn[];
  /**
   * Array of query result rows - must match column order
   * Example:
   * [ ['Jean', 'Frissilla', 'Italy'],
   *   ['John', 'Johnson', 'US'],
   *   ['Juan', 'Bautista', 'Brazil'],
   *   ['Jordan', 'Dristol', 'Ontario']
   * ]
   */
  queryResultRows: string[][];
}

interface IColumn {
  id: string;
  label: string;
}

// Column Headers - uses the label
const getColumns = (cols: IColumn[]) => {
  return cols.map(col => ({
    title: col.label==='id' ? 'id_' : col.label,
  }));
};

// Determine className property, based on the length of the cell value
const getClassName = (value: string) => {
  return value
    ? value.length > 100
      ? 'sql-results-table__largeColWidth'
      : value.length > 20
      ? 'sql-results-table__mediumColWidth'
      : value.length > 10
      ? 'sql-results-table__smallColWidth'
      : value.length > 2
      ? 'sql-results-table__minimumColWidth'
      : ''
    : '';
};

// row cells are sized based upon the data value lengths
const getSizedRows = (rows: string[][]) => {
  const sizedRows: IRow[][] = [];
  for (const row of rows) {
    const rowValues: IRow[] = [];
    for (const rowValue of row) {
      const theValue = {
        props: {
          className: getClassName(rowValue),
        },
        title: rowValue,
      } as IRow;
      rowValues.push(theValue);
    }
    sizedRows.push(rowValues);
  }
  return sizedRows.length > 0 ? sizedRows : [];
};

/**
 * The SqlResultsTable.  The rows are sized based on cell data lengths.
 */
export const SqlResultsTable: React.FunctionComponent<ISqlResultsTableProps> = props => {
  return (
    <Table
      aria-label="SQL Results Table"
      className='sql-results-table'
      variant={TableVariant.compact}
      cells={getColumns(props.queryResultCols)}
      rows={getSizedRows(props.queryResultRows)}
    >
      <TableHeader />
      <TableBody />
    </Table>
  );
};
