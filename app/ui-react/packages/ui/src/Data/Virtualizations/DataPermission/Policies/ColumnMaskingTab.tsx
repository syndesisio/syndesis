import { Button, PageSection } from '@patternfly/react-core';
import { PlusCircleIcon } from '@patternfly/react-icons';
import React from 'react';
import { ColumnMaskingItems } from '..';
import './ColumnMaskingTab.css';

interface IColumnMaskingTabProps {
  i18nAddACondition: string;
  i18nAddColumnMasking: string;
  i18nDeleteCondition: string;
  i18nMaskings: string;
  i18nMaskingLabel: string;
  i18nOrder: string;
  i18nSelectColumn: string;
  i18nUserRole: string;
  i18nRemoveRow: string;
  i18nSelectRole: string;
  i18nColumn: string;
  i18nCondition: string;
  i18nDelete: string;
  i18nValidate: string;
  updateColumnMaskCount: (val: number) => void;
}

const getRandomId = () => {
  return Math.floor(Math.random() * 100000);
};

export const ColumnMaskingTab: React.FunctionComponent<IColumnMaskingTabProps> = props => {
  const [columnMaskingItems, setColumnMaskingItems] = React.useState<number[]>([
    0,
  ]);

  const addrow = () => {
    const columnMaskingItemsCopy = [...columnMaskingItems];
    const rowId = getRandomId();
    columnMaskingItemsCopy.push(rowId);
    setColumnMaskingItems(columnMaskingItemsCopy);
  };

  const removeRow = (index: number) => {
    const columnMaskingItemsCopy = [...columnMaskingItems];
    const indexRemove = columnMaskingItemsCopy.indexOf(index);
    columnMaskingItemsCopy.splice(indexRemove, 1);
    setColumnMaskingItems(columnMaskingItemsCopy);
  };

  React.useEffect(() => {
    props.updateColumnMaskCount(columnMaskingItems.length);
  }, [columnMaskingItems]);

  return (
    <PageSection noPadding={false} className={'column-masking-tab-content'}>
      {columnMaskingItems.map((columnMaskingItem, index) => (
        <ColumnMaskingItems
          key={columnMaskingItem + index}
          i18nAddACondition={props.i18nAddACondition}
          i18nDeleteCondition={props.i18nDeleteCondition}
          i18nMaskings={props.i18nMaskings}
          i18nMaskingLabel={props.i18nMaskingLabel}
          i18nOrder={props.i18nOrder}
          i18nColumn={props.i18nColumn}
          i18nSelectColumn={props.i18nSelectColumn}
          i18nUserRole={props.i18nUserRole}
          i18nRemoveRow={props.i18nRemoveRow}
          i18nSelectRole={props.i18nSelectRole}
          i18nCondition={props.i18nCondition}
          i18nValidate={props.i18nValidate}
          i18nDelete={props.i18nDelete}
          index={columnMaskingItem}
          order={index}
          removeRow={removeRow}
        />
      ))}
      <Button
        variant="link"
        icon={<PlusCircleIcon />}
        onClick={addrow}
        className={'column-masking-tab-add_button'}
      >
        {props.i18nAddColumnMasking}
      </Button>
    </PageSection>
  );
};
