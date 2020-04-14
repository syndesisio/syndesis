import { Flex, FlexItem, Text } from '@patternfly/react-core';
import {
  Table,
  TableBody,
  TableHeader,
  TableVariant
} from '@patternfly/react-table';
import * as React from 'react';
import { toValidHtmlId } from '../../helpers';

export interface IExtensionIntegration {
  /**
   * ID param is used to create link to Integration Details page
   */
  id: string;
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

/**
 * Extension Integrations Table
 * You can view see this component in the Extension detail page
 * when an extension has been used at least once in an existing integration.
 * @param props
 * @constructor
 */
export const ExtensionIntegrationsTable: React.FunctionComponent<
  IExtensionIntegrationsTableProps
  > = props => {

  const rows = () => {
    const onIntegrationSelected = (integrationId: string) => {
      props.onSelectIntegration(integrationId);
    };

    const newRows = props.data.map((integration, integrationIndex) => {
      /**
       * Necessary to pass event due to the following issue:
       * https://github.com/facebook/react/issues/16382#issuecomment-530911232
       * @param e
       */
      const onClick = (e: any) => {
        e.preventDefault();
        onIntegrationSelected(integration.id);
        return false;
      };

      return [
        {
          cells: [
            {
              title: (
                <Flex>
                  <FlexItem key={integrationIndex + '-name'}>
                    <a
                      data-testid={`extension-integrations-table-${toValidHtmlId(
                        integration.name
                      )}-integration-link`}
                      href={'#'}
                      onClick={onClick}
                    >
                      {integration.name}
                    </a>
                  </FlexItem>
                </Flex>
              )
            },
            {
              title: (
                <Flex>
                  <FlexItem key={integrationIndex + '-description'}>
                    <span>{integration.description}</span>
                  </FlexItem>
                </Flex>
              )
            }
          ],
        }
      ];
    });

    return newRows.reduce((a, b) => a.concat(b), []);
  };

  const columns = [
    props.i18nName,
    props.i18nDescription
  ];

  return (
    <div className={'extension-group'}>
      <Text>{props.i18nUsageMessage}</Text>
      {props.data.length !== 0 ? (
        <Table
          aria-label={'extension-integrations-table'}
          cells={columns}
          rows={rows()}
          variant={TableVariant.compact}
        >
          <TableHeader />
          <TableBody />
        </Table>
      ) : null}
    </div>
  );
};
