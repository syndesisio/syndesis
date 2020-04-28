import {
  IRow,
  Table,
  TableBody,
  TableHeader,
  TableVariant
} from '@patternfly/react-table';
import * as React from 'react';

export interface IIntegrationExtension {
  description: string;
  lastUpdated: string;
  name: string;
}

export interface IIntegrationEditorExtensionListProps {
  extensionsAvailable: IIntegrationExtension[];
  extensionNamesSelected: string[];
  handleSelectAll: (isSelected: boolean, extensionList: IIntegrationExtension[]) => void;
  i18nHeaderDescription: string;
  i18nHeaderLastUpdated: string;
  i18nHeaderName: string;
  i18nTableDescription: string;
  i18nTableName: string;
  onSelect: (extensionName: string, selected: boolean) => void;
}

export const IntegrationEditorExtensionList: React.FunctionComponent<IIntegrationEditorExtensionListProps> = (
  {
    extensionsAvailable,
    extensionNamesSelected,
    handleSelectAll,
    i18nHeaderDescription,
    i18nHeaderLastUpdated,
    i18nHeaderName,
    i18nTableDescription,
    i18nTableName,
    onSelect
  }) => {
  const columns = [
    i18nHeaderName,
    i18nHeaderDescription,
    i18nHeaderLastUpdated,
  ];

  const getTableRows = () => {
    const tableRows: IRow[] = [];

    if (extensionsAvailable && extensionsAvailable.length) {
      for (const extension of extensionsAvailable) {
        tableRows.push({
          cells: [extension.name, extension.description, extension.lastUpdated]
        });

        /**
         * Check for pre-selected names
         */
        if (extensionNamesSelected.includes(extension.name)) {
          tableRows[tableRows.length - 1].selected = true;
        }
      }
    }

    return tableRows.length > 0 ? tableRows : [];
  };

  let rowsList = getTableRows();

  const [rowUpdate, setRowUpdate] = React.useState(false);

  React.useEffect(() => {
    rowsList = getTableRows();
  }, [rowUpdate]);

  const onChange = (event: any, isSelected: boolean, rowIndex: number) => {
    let rows;

    if (rowIndex === -1) {
      /**
       * Handle "select all"
       */
      rows = rowsList.map(oneRow => {
        oneRow.selected = isSelected;
        return oneRow;
      });
      handleSelectAll(isSelected, extensionsAvailable);
    } else {
      /**
       * Handle single selection
       */
      rows = [...rowsList];
      rows[rowIndex].selected = isSelected;
      onSelect(extensionsAvailable[rowIndex].name, isSelected);
    }

    setRowUpdate(!rowUpdate);
  };
  
  return (
    <>
      <h2>{i18nTableName}</h2>
      <p>{i18nTableDescription}</p>
      <Table aria-label="Integration Extension Table"
             canSelectAll={true}
             cells={columns}
             data-testid={'integration-extension-list-table'}
             onSelect={onChange}
             rows={rowsList}
             variant={TableVariant.compact}
      >
        <TableHeader/>
        <TableBody/>
      </Table>
    </>
  );
}
