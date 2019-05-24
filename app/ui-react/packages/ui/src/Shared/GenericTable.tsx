import { Table } from 'patternfly-react';
import * as React from 'react';

/**
 * This component wraps a generic table component and exposes a simple and
 * consistent methodology to customize basic presentation properties and provide
 * an interface of setting the row/column data within the table.
 */

export type IGenericTableFormatter = (value: any) => React.ReactNode;

export interface IGenericTableColumn {
  cell: {
    formatters: IGenericTableFormatter[];
  };
  header: {
    formatters: IGenericTableFormatter[];
    label: string;
  };
  property: string;
}

export interface IGenericTableStyle {
  striped?: boolean;
  bordered?: boolean;
  hover?: boolean;
}

export interface IGenericTableData {
  columns: IGenericTableColumn[];
  rows: Array<{ [property: string]: any }>;
  rowKey: string;
}

export const DefaultHeaderFormat = (value: any) => (
  <Table.Heading>{value}</Table.Heading>
);
export const DefaultCellFormat = (value: any) => (
  <Table.Cell>{value}</Table.Cell>
);

export const GenericTable: React.FC<IGenericTableStyle & IGenericTableData> = ({
  striped = true,
  bordered = true,
  hover = true,
  columns,
  rows,
  rowKey,
}) => (
  <Table.PfProvider
    striped={striped}
    bordered={bordered}
    hover={hover}
    columns={columns}
  >
    <Table.Header />
    <Table.Body rows={rows} rowKey={rowKey} />
  </Table.PfProvider>
);
