import { Col, Icon, ListView, Row, Table } from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationDetailActivityItemProps {
  date: string;
  errorCount: number;
  i18nErrorsFound: string;
  i18nHeaderStep?: string;
  i18nHeaderTime?: string;
  i18nHeaderDuration?: string;
  i18nHeaderDurationUnit?: string;
  i18nHeaderStatus?: string;
  i18nHeaderOutput?: string;
  i18nNoErrors: string;
  i18nNoOutput?: string;
  i18nNoSteps: string;
  i18nVersion: string;
  steps: JSX.Element[];
  time: string;
  version?: number;
}

export class IntegrationDetailActivityItem extends React.Component<
  IIntegrationDetailActivityItemProps
> {
  public render() {
    const headerFormat = (value: string) => (
      <Table.Heading>{value}</Table.Heading>
    );

    const cellFormat = (value: string) => <Table.Cell>{value}</Table.Cell>;

    return (
      <ListView.Item
        key={1}
        actions={
          <>
            {this.props.errorCount > 0 ? (
              <span>
                <Icon type="pf" name="error-circle-o" />
                {'  '}
                {this.props.i18nErrorsFound}
              </span>
            ) : (
              <span>
                <Icon type="pf" name="ok" />
                {'  '}
                {this.props.i18nNoErrors}
              </span>
            )}
          </>
        }
        heading={this.props.date}
        description={this.props.time}
        additionalInfo={[
          <React.Fragment key={'not-really-needed'}>
            {this.props.i18nVersion}
            {'  '}
            {this.props.version}
          </React.Fragment>,
        ]}
      >
        <Row>
          {this.props.steps ? (
            <Col sm={11}>
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
                {this.props.steps}
              </Table.PfProvider>
            </Col>
          ) : (
            <Col sm={11}>
              <span>{this.props.i18nNoSteps}</span>
            </Col>
          )}
        </Row>
      </ListView.Item>
    );
  }
}
