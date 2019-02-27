import { Row, Table } from 'patternfly-react';
import * as React from 'react';

export interface IExtensionIntegration {
  name: string;
  description: string;
}

export interface IExtensionIntegrationsTableProps {
  i18nDescription: string;
  i18nName: string;
  i18nUsageMessage: string;
  onSelectIntegration: (integrationId: string) => void;
  data: IExtensionIntegration[];
}

export class ExtensionIntegrationsTable extends React.Component<
  IExtensionIntegrationsTableProps
> {
  public getColumns() {
    const headerFormat = (value: string) => (
      <Table.Heading>{value}</Table.Heading>
    );

    const clickableValueFormat = (
      value: string,
      { rowData }: { rowData: any }
    ) => {
      // rowData is an Integration type so 'name' property is what makes the integration unique
      const onClick = () => this.onIntegrationSelected(rowData.name);
      return (
        <Table.Cell>
          <a href="javascript:void(0)" onClick={onClick}>
            {value}
          </a>
        </Table.Cell>
      );
    };

    // Creates 2 columns:
    // 1. first column is integration name
    // 2. second column is optional integration description.
    return [
      {
        cell: {
          formatters: [clickableValueFormat],
        },
        header: {
          formatters: [headerFormat],
          label: this.props.i18nName,
        },
        property: 'name', // must match the name of the IntegrationOverview.name property
      },
      {
        cell: {
          formatters: [(value: string) => <Table.Cell>{value}</Table.Cell>],
        },
        header: {
          formatters: [headerFormat],
          label: this.props.i18nDescription,
        },
        property: 'description', // must match the name of the IntegrationOverview.description property
      },
    ];
  }

  public onIntegrationSelected(integrationId: string) {
    this.props.onSelectIntegration(integrationId);
  }

  public render() {
    return (
      <div className="extension-group">
        <Row>
          <div className="col-xs-offset-1 col-xs-11">
            <p>{this.props.i18nUsageMessage}</p>
            {this.props.data.length !== 0 ? (
              <Table.PfProvider
                striped={true}
                bordered={true}
                hover={true}
                columns={this.getColumns()}
              >
                <Table.Header />
                <Table.Body rows={this.props.data} rowKey="name" />
              </Table.PfProvider>
            ) : null}
          </div>
        </Row>
      </div>
    );
  }
}
