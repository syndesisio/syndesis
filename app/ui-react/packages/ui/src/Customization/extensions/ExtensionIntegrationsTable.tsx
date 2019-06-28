import { Text } from '@patternfly/react-core';
import { Table } from 'patternfly-react';
import * as React from 'react';
import { toValidHtmlId } from '../../helpers';

export interface IExtensionIntegration {
  id: string; // used to create link to integration details page
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

export const ExtensionIntegrationsTable: React.FunctionComponent<
  IExtensionIntegrationsTableProps
> = props => {
  const getColumns = () => {
    const headerFormat = (value: string) => (
      <Table.Heading>{value}</Table.Heading>
    );

    const clickableValueFormat = (
      value: string,
      { rowData }: { rowData: any }
    ) => {
      // rowData is an Integration type so 'name' property is what makes the integration unique
      const onClick = () => onIntegrationSelected(rowData.id);
      return (
        <Table.Cell>
          <a
            data-testid={`extension-integrations-table-${toValidHtmlId(
              rowData.name
            )}-integration-link`}
            href="javascript:void(0)"
            onClick={onClick}
          >
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
          label: props.i18nName,
        },
        property: 'name', // must match the name of the IntegrationOverview.name property
      },
      {
        cell: {
          formatters: [(value: string) => <Table.Cell>{value}</Table.Cell>],
        },
        header: {
          formatters: [headerFormat],
          label: props.i18nDescription,
        },
        property: 'description', // must match the name of the IntegrationOverview.description property
      },
    ];
  };

  const onIntegrationSelected = (integrationId: string) => {
    props.onSelectIntegration(integrationId);
  };

  return (
    <div className="extension-group">
      <Text>{props.i18nUsageMessage}</Text>
      {props.data.length !== 0 ? (
        <Table.PfProvider
          striped={true}
          bordered={true}
          hover={true}
          columns={getColumns()}
        >
          <Table.Header />
          <Table.Body rows={props.data} rowKey="name" />
        </Table.PfProvider>
      ) : null}
    </div>
  );
};
