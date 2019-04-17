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
                property: 'step',
              },
              header: {
                formatters: [headerFormat],
                label: 'Step',
              },
            },
            {
              cell: {
                formatters: [cellFormat],
                property: 'time',
              },
              header: {
                formatters: [headerFormat],
                label: 'Time',
              },
            },
            {
              cell: {
                formatters: [cellFormat],
                property: 'duration',
              },
              header: {
                formatters: [headerFormat],
                label: 'Duration',
              },
            },
            {
              cell: {
                formatters: [cellFormat],
                property: 'status',
              },
              header: {
                formatters: [headerFormat],
                label: 'Status',
              },
            },
            {
              cell: {
                formatters: [cellFormat],
                property: 'output',
              },
              header: {
                formatters: [headerFormat],
                label: 'Output',
              },
            },
          ]}
        >
          <Table.Header />
          <Table.Body
            rows={[
              {
                step: this.props.name,
                time: this.props.time,
                duration: this.props.duration,
                status: this.props.status,
                output: this.props.output,
              },
            ]}
            rowKey={this.props.name}
          />
        </Table.PfProvider>
        {this.props.children}
      </>
    );
  }
}
