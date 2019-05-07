import { withKnobs } from '@storybook/addon-knobs';
import { storiesOf } from '@storybook/react';
import { Table } from 'patternfly-react';
import * as React from 'react';
import { GenericTable } from '../../src';

const stories = storiesOf('Shared/GenericTable', module);
stories.addDecorator(withKnobs);

const resultCols = [
  { id: 'FirstName', label: 'First Name' },
  { id: 'LastName', label: 'Last Name' },
  { id: 'Country', label: 'Country' },
];
const resultRows = [
  { FirstName: 'Jean', LastName: 'Frissilla', Country: 'Italy' },
  { FirstName: 'John', LastName: 'Johnson', Country: 'US' },
  { FirstName: 'Juan', LastName: 'Bautista', Country: 'Brazil' },
  { FirstName: 'Jordan', LastName: 'Dristol', Country: 'Ontario' },
  { FirstName: 'Jenny', LastName: 'Clayton', Country: 'US' },
  { FirstName: 'Jorge', LastName: 'Rivera', Country: 'Mexico' },
  { FirstName: 'Jake', LastName: 'Klein', Country: 'US' },
  { FirstName: 'Julia', LastName: 'Zhang', Country: 'China' },
];

const resultCols2 = [
  { id: 'FirstName', label: 'First Name' },
  { id: 'LastName', label: 'Last Name' },
  { id: 'Age', label: 'Age' },
  { id: 'ID', label: 'SSN' },
  { id: 'City', label: 'City' },
  { id: 'Country', label: 'Country' },
];
const resultRows2 = [
  {
    FirstName: 'Jean',
    LastName: 'Frissilla',
    Age: '43',
    ID: '111111111',
    City: 'Florence',
    Country: 'Italy',
  },
  {
    FirstName: 'John',
    LastName: 'Johnson',
    Age: '25',
    ID: '222222222',
    City: 'Jackson',
    Country: 'US',
  },
  {
    FirstName: 'Juan',
    LastName: 'Bautista',
    Age: '37',
    ID: '333333333',
    City: 'Sao Paulo',
    Country: 'Brazil',
  },
];

const defaultCellFormat = (value: any) => (
  <Table.Heading>{value}</Table.Heading>
);
const defaultHeaderFormat = (value: any) => <Table.Cell>{value}</Table.Cell>;

stories.add('3Columns-8Rows', () => (
  <GenericTable
    columns={resultCols.map(col => ({
      cell: {
        formatters: [defaultCellFormat],
      },
      header: {
        formatters: [defaultHeaderFormat],
        label: col.label,
      },
      property: col.id,
    }))}
    rows={resultRows}
    rowKey={resultCols.length > 0 ? resultCols[0].id : ''}
  />
));

stories.add('6Columns-3Rows', () => (
  <GenericTable
    columns={resultCols2.map(col => ({
      cell: {
        formatters: [defaultCellFormat],
      },
      header: {
        formatters: [defaultHeaderFormat],
        label: col.label,
      },
      property: col.id,
    }))}
    rows={resultRows2}
    rowKey={resultCols2.length > 0 ? resultCols2[0].id : ''}
  />
));
