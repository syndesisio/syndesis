import { WithExtensionIntegrations } from '@syndesis/api';
import { Loader } from '@syndesis/ui';
import { WithLoader } from '@syndesis/utils';
import { Row, Table } from 'patternfly-react';
import * as React from 'react';
import { NamespacesConsumer } from 'react-i18next';

export interface IExtensionIntegrationsProps {
  extensionId: string;
  i18nDescription: string;
  i18nName: string;
  i18nUsageMessage: string;
  onSelectIntegration: (integrationId: string) => void;
}

/**
 * A component that lists the integrations that uses a specific extension.
 */
export default class ExtensionIntegrations extends React.Component<
  IExtensionIntegrationsProps
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
      <WithExtensionIntegrations extensionId={this.props.extensionId}>
        {({ data, hasData, error }) => (
          <WithLoader
            error={error}
            loading={!hasData}
            loaderChildren={<Loader />}
            errorChildren={<div>TODO</div>}
          >
            {() => (
              <NamespacesConsumer ns={['customizations', 'shared']}>
                {t => (
                  <div className="extension-group">
                    <Row>
                      <div className="col-xs-offset-1 col-xs-11">
                        <p>{this.props.i18nUsageMessage}</p>
                        {data.length !== 0 ? (
                          <Table.PfProvider
                            striped={true}
                            bordered={true}
                            hover={true}
                            columns={this.getColumns()}
                          >
                            <Table.Header />
                            <Table.Body rows={data} rowKey="name" />
                          </Table.PfProvider>
                        ) : null}
                      </div>
                    </Row>
                  </div>
                )}
              </NamespacesConsumer>
            )}
          </WithLoader>
        )}
      </WithExtensionIntegrations>
    );
  }
}
