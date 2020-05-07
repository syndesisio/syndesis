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
  extensionId: string;
  lastUpdated: number;
  name: string;
  selected?: boolean;
}

export interface IIntegrationEditorExtensionTableRowsProps {
  /**
   * List of extensions available as provided from
   * the API.
   */
  extensionsAvailable: IExtensionProps[];
  /**
   * These are provided by the API, and determine
   * which rows will be selected on page load.
   */
  extensionIdsSelected: string[];
  i18nHeaderDescription: string;
  i18nHeaderLastUpdated: string;
  i18nHeaderName: string;
  i18nTableDescription: string;
  i18nTableName: string;
  onSelect: (extensionId: string, selected: boolean) => void;
  onSelectAll: (extensionIds: string[]) => void;
}

export const IntegrationEditorExtensionTableRows: React.FunctionComponent<IIntegrationEditorExtensionTableRowsProps> = (
  {
    extensionsAvailable,
    extensionIdsSelected,
    i18nHeaderDescription,
    i18nHeaderLastUpdated,
    i18nHeaderName,
    i18nTableDescription,
    i18nTableName,
    onSelect,
    onSelectAll
  }) => {
  const columns = [
    i18nHeaderName,
    i18nHeaderDescription,
    i18nHeaderLastUpdated,
  ];

  let rows;

  const getTableRows = React.useCallback(() => {
    const tableRows: IRow[] = [];

    for (const extension of extensionsAvailable) {
      const options = { year: "numeric", month: "long", day: "numeric" };

      const formattedDate = extension.lastUpdated
        ? new Date(extension.lastUpdated).toLocaleString(undefined, options)
        : "";

      /**
       * Create format for PF tables, check for pre-selected
       * extensions by their IDs.
       * This array includes props used to determine state of
       * the row in the UI, irrespective of the extension itself.
       * We must also maintain an array of extension IDs that
       * have been selected, to provide back to the API.
       * The story or @syndesis module maintains the state for
       * this list separately.
       */
      tableRows.push({
        cells: [extension.name, extension.description, formattedDate],
        meta: {extensionId: extension.extensionId},
        selected: extensionIdsSelected.includes(extension.extensionId)
      });
    }

    return tableRows.length > 0 ? tableRows : [];
  }, [extensionIdsSelected, extensionsAvailable]);

  const rowsList = React.useMemo(getTableRows, [getTableRows]);

  /**
   * Fired when a row is selected.
   * @param event
   * @param isSelected
   * @param rowIndex
   * @param rowData
   */
  const onTableRowChange = (
    event: any,
    isSelected: boolean,
    rowIndex: number,
    rowData: any
  ) => {
    let newIdSelectionArray: string[] = [];

    if (rowIndex === -1) {
      /**
       * Handle "select all" in rows
       */
      rows = rowsList.map(oneRow => {
        oneRow.selected = isSelected;
        return oneRow;
      });

      if (isSelected) {
        newIdSelectionArray = extensionsAvailable.map(extension => extension.extensionId);
        onSelectAll(newIdSelectionArray);
      } else {
        newIdSelectionArray = [];
        /**
         * Parent component callback with new data
         */
        onSelectAll(newIdSelectionArray);
      }
    } else {
      /**
       * Handle single selection in rows
       */
      const { meta } = rowData;

      rows = [...rowsList];
      rows[rowIndex].selected = isSelected;

      /**
       * Parent component callback with new data
       */
      onSelect(meta.extensionId, isSelected);
    }
  };

  return (
    <>
      <h2>{i18nTableName}</h2>
      <p>{i18nTableDescription}</p>
      <Table aria-label="Integration Extension Table"
             canSelectAll={true}
             cells={columns}
             data-testid={'integration-extension-list-table'}
             onSelect={onTableRowChange}
             rows={rowsList}
             variant={TableVariant.compact}
      >
        <TableHeader/>
        <TableBody/>
      </Table>
    </>
  );
}
