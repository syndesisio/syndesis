import * as React from 'react';
import { useEffect } from 'react';
import { IntegrationEditorExtensionTableRows } from './IntegrationEditorExtensionTableRows';

interface IExtensionProps {
  description: string;
  extensionId: string;
  lastUpdated: number;
  name: string;
}

export interface IIntegrationEditorExtensionTableProps {
  /**
   * List of extensions available as provided from
   * the API.
   */
  extensionsAvailable: IExtensionProps[];
  i18nHeaderDescription: string;
  i18nHeaderLastUpdated: string;
  i18nHeaderName: string;
  i18nTableDescription: string;
  i18nTableName: string;
  /**
   * When a change is detected and a row is selected,
   * both the UI row state and @syndesis or story component
   * state are updated. This is the function that is
   * called to maintain the latter.
   * @param extensionId
   * @param selected
   */
  onSelect: (extensionIds: string[]) => void;
  /**
   * These are provided by the API, and determine
   * which rows will be selected on page load.
   * The state of these is maintained separately
   * from the UI's state of the rows, because the format
   * required for PF react table is different to that
   * expected by the API once the integration is saved.
   */
  preSelectedExtensionIds: string[];
}

export const IntegrationEditorExtensionTable: React.FunctionComponent<IIntegrationEditorExtensionTableProps> = (
  {
    extensionsAvailable,
    i18nHeaderDescription,
    i18nHeaderLastUpdated,
    i18nHeaderName,
    i18nTableDescription,
    i18nTableName,
    onSelect,
    preSelectedExtensionIds
  }) => {
  /**
   * Table state for array of IDs for selected extensions,
   * starting with the preselected list
   */
  const [selectedExtensionIds, setSelectedExtensionIds] = React.useState<string[]>(
    preSelectedExtensionIds
  );

  const handleSelectExtension = (extensionId: string) => {
    /**
     * Make a shallow copy of selectedExtensions array
     */
    const tempArray = selectedExtensionIds.slice();
    if (!tempArray.includes(extensionId)) {
      tempArray.push(extensionId);
    }

    setSelectedExtensionIds(tempArray);
  };

  const handleDeselectExtension = (extensionId: string) => {
    /**
     * Make a shallow copy of selectedExtensions array,
     * then find the index of the selected id
     */
    const tempArray = selectedExtensionIds.filter(id => id !== extensionId);
    setSelectedExtensionIds(tempArray);
  };

  const onTableChange = React.useCallback((extensionId: string, isSelected: boolean) => {
    if (isSelected) {
      handleSelectExtension(extensionId);
    } else {
      handleDeselectExtension(extensionId);
    }
  }, [handleSelectExtension, handleDeselectExtension]);

  const onTableChangeAll = React.useCallback((newList: string[]) => {
    setSelectedExtensionIds(newList);
  }, [setSelectedExtensionIds]);

  const callOnSelect = () => {
    onSelect(selectedExtensionIds);
  };

  useEffect(() => {
    callOnSelect();
  }, [callOnSelect]);

  return (
    <IntegrationEditorExtensionTableRows
      extensionsAvailable={extensionsAvailable}
      extensionIdsSelected={preSelectedExtensionIds}
      i18nHeaderDescription={i18nHeaderDescription}
      i18nHeaderLastUpdated={i18nHeaderLastUpdated}
      i18nHeaderName={i18nHeaderName}
      i18nTableDescription={i18nTableDescription}
      i18nTableName={i18nTableName}
      onSelect={onTableChange}
      onSelectAll={onTableChangeAll}
    />
  );
}
