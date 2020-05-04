import {
  IRow,
  Table,
  TableBody,
  TableHeader,
  TableVariant
} from '@patternfly/react-table';
import * as React from 'react';

interface IExtensionProps {
  description: string;
  lastUpdated: string;
  name: string;
  selected?: boolean;
}

export interface IIntegrationEditorExtensionListProps {
  extensionsAvailable: IExtensionProps[];
  extensionNamesSelected: string[];
  handleSelectAll: (isSelected: boolean, extensionList: IExtensionProps[]) => void;
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
    i18nHeaderDescription,
    i18nHeaderLastUpdated,
    i18nHeaderName,
    i18nTableDescription,
    i18nTableName,
  }) => {
  const [selectedExtensions, setSelectedExtensions] = React.useState<IExtensionProps[]>([]);

  const columns = [
    i18nHeaderName,
    i18nHeaderDescription,
    i18nHeaderLastUpdated,
  ];


  const handleSelectExtension = (extension: IExtensionProps) => {
    const currentlySelected = selectedExtensions.slice();
    currentlySelected.push(extension);
    setSelectedExtensions(currentlySelected);
  };

  const handleDeselectExtension = (extensionName: string) => {
    const currentlySelected = selectedExtensions.slice();
    const index = currentlySelected.findIndex(
      extension => extension.name === extensionName
    );

    if (index !== -1) {
      currentlySelected.splice(index, 1);
    }

    setSelectedExtensions(currentlySelected);
  };

  const clearExtensionSelection = () => {
    setSelectedExtensions([]);
  };

  const handleSelectAll = (
    isSelected: boolean,
    extensionList?: IExtensionProps[]
  ) => {
    if (isSelected && extensionList) {
      setSelectedExtensions(extensionList);
    } else {
      clearExtensionSelection();
    }
  };

  /*
  const getSelectedExtensionName = (selectedExtensionsList: IExtensionProps[]): string[] => {
    return selectedExtensionsList.map(extension => extension.name);
  };

  const selectedExtensionNames: string[] = getSelectedExtensionName(selectedExtensions);

   */

  const onSelect = (extensionName: string, selected: boolean) => {
    if (selected) {
      for (const extensionInfo of extensionsAvailable) {
        if (extensionInfo.name === extensionName) {
          handleSelectExtension(extensionInfo);
        }
      }
    } else {
      handleDeselectExtension(extensionName);
    }
  };


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
