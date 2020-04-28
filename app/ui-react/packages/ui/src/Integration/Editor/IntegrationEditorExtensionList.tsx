import {
  Table,
  TableBody,
  TableHeader,
  TableVariant
} from '@patternfly/react-table';
import * as React from 'react';

export interface IIntegrationEditorExtensionListProps {
  extensions: any;
  i18nHeaderDescription: string;
  i18nHeaderLastUpdate: string;
  i18nHeaderName: string;
}

export const IntegrationEditorExtensionList: React.FunctionComponent<IIntegrationEditorExtensionListProps> = (
  {
    extensions,
    i18nHeaderDescription,
    i18nHeaderLastUpdate,
    i18nHeaderName
  }) => {
  const rows = () => {
    const newRows = extensions.map((extension: any) => {
      return [{
        cells: [
          extension.name,
          extension.description,
          extension.lastUpdate
        ],
      }];
    });

    return newRows.reduce((a: any, b: any) => a.concat(b), []);
  };

  const columns = [
    i18nHeaderName,
    i18nHeaderDescription,
    i18nHeaderLastUpdate,
  ];

  return (
    <Table aria-label="Integration Extension Table"
           cells={columns}
           data-testid={'integration-extension-list-table'}
           rows={rows()}
           variant={TableVariant.compact}
    >
      <TableHeader />
      <TableBody />
    </Table>
  );
}
