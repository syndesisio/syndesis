import { Table } from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationDetailActivityItemStepsProps {
  i18nHeaderStep?: string;
  i18nHeaderTime?: string;
  i18nHeaderDuration?: string;
  i18nHeaderDurationUnit?: string;
  i18nHeaderStatus?: string;
  i18nHeaderOutput?: string;
  i18nNoOutput?: string;
  name?: string;
  time?: string;
  duration?: number;
  status?: string;
  output?: string;
}

export class IntegrationDetailActivityItemSteps extends React.Component<
  IIntegrationDetailActivityItemStepsProps
> {
  public render() {
    const headerFormat = (value: string) => (
      <Table.Heading>{value}</Table.Heading>
    );

    const cellFormat = (value: string) => <Table.Cell>{value}</Table.Cell>;

    return (
      <>
        <Table.PfProvider
          striped={true}
          bordered={true}
          hover={true}
          columns={[
            {
              cell: {
                formatters: [cellFormat],
                property: 'first_name',
              },
              header: {
                formatters: [headerFormat],
                label: 'First Name',
              },
            },
            {
              cell: {
                formatters: [cellFormat],
                property: 'last_name',
              },
              header: {
                formatters: [headerFormat],
                label: 'Last Name',
              },
            },
            {
              cell: {
                formatters: [cellFormat],
                property: 'username',
              },
              header: {
                formatters: [headerFormat],
                label: 'Username',
              },
            },
          ]}
        >
          <Table.Header />
          <Table.Body
            rows={[
              {
                first_name: 'Dan',
                id: 0,
                last_name: 'Abramov',
              },
              {
                first_name: 'Sebastian',
                id: 1,
                last_name: 'Markbåge',
              },
              {
                first_name: 'Sophie',
                id: 2,
                last_name: 'Alpert',
              },
            ]}
            rowKey="id"
          />
        </Table.PfProvider>
      </>
    );
  }
}
