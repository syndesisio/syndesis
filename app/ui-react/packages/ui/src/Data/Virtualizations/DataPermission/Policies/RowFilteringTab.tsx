import { Button, PageSection } from '@patternfly/react-core';
import { PlusCircleIcon } from '@patternfly/react-icons';
import React from 'react';
import { RowFilteringItem } from '..';
import './RowFilteringTab.css';

// tslint:disable-next-line: no-empty-interface
interface IRowFilteringTabProps {
  i18nAddPolicy: string;
  i18nCondition: string;
  i18nOperation: string;
  i18nSelectOperation: string;
  i18nUserRole: string;
  i18nValidate: string;
  i18nRemoveRow: string;
  i18nSelectRole: string;
}
const getRandomId = () => {
  return Math.floor(Math.random() * 100000);
};
export const RowFilteringTab: React.FunctionComponent<IRowFilteringTabProps> = props => {
  const [rowFilteringItems, setRowFilteringItems] = React.useState<number[]>([
    0,
  ]);

  const addrow = () => {
    const rowFilteringItemsCopy = [...rowFilteringItems];
    const rowId = getRandomId();
    rowFilteringItemsCopy.push(rowId);
    setRowFilteringItems(rowFilteringItemsCopy);
  };

  const removeRow = (index: number) => {
    const rowFilteringItemsCopy = [...rowFilteringItems];
    const indexRemove = rowFilteringItemsCopy.indexOf(index);
    rowFilteringItemsCopy.splice(indexRemove, 1);
    setRowFilteringItems(rowFilteringItemsCopy);
  };

  return (
    <PageSection noPadding={false} className={'row-filtering-tab-section'}>
      {rowFilteringItems.map(rowFilteringItem => (
        <RowFilteringItem
          key={rowFilteringItem}
          index={rowFilteringItem}
          removeRow={removeRow}
          i18nCondition={props.i18nCondition}
          i18nOperation={props.i18nOperation}
          i18nSelectOperation={props.i18nSelectOperation}
          i18nUserRole={props.i18nUserRole}
          i18nValidate={props.i18nValidate}
          i18nRemoveRow={props.i18nRemoveRow}
          i18nSelectRole={props.i18nSelectRole}
        />
      ))}
      <Button variant="link" icon={<PlusCircleIcon />} onClick={addrow}>
        {props.i18nAddPolicy}
      </Button>
    </PageSection>
  );
};
