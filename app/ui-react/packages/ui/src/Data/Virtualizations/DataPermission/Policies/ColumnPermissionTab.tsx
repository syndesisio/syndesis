import { Button, PageSection } from '@patternfly/react-core';
import { PlusCircleIcon } from '@patternfly/react-icons';
import React from 'react';
import { ColumnPermissionItem } from '..';
import './ColumnPermissionTab.css';

interface IColumnPermissionTabProps {
  i18nSelectColumn: string;
  i18nUserRole: string;
  i18nRemoveRow: string;
  i18nSelectRole: string;
  i18nPermission: string;
  i18nAddPermission: string;
  i18nColumn: string;
  updateColumnPermissionCount: (val: number) => void;
}
const getRandomId = () => {
  return Math.floor(Math.random() * 100000);
};
export const ColumnPermissionTab: React.FunctionComponent<IColumnPermissionTabProps> = props => {
  const [columnPermissionItems, setColumnPermissionItems] = React.useState<
    number[]
  >([0]);

  const addrow = () => {
    const columnPermissionItemsCopy = [...columnPermissionItems];
    const rowId = getRandomId();
    columnPermissionItemsCopy.push(rowId);
    setColumnPermissionItems(columnPermissionItemsCopy);
  };

  const removeRow = (index: number) => {
    const columnPermissionItemsCopy = [...columnPermissionItems];
    const indexRemove = columnPermissionItemsCopy.indexOf(index);
    columnPermissionItemsCopy.splice(indexRemove, 1);
    setColumnPermissionItems(columnPermissionItemsCopy);
  };

  React.useEffect(() => {
    props.updateColumnPermissionCount(columnPermissionItems.length);
  }, [columnPermissionItems]);

  return (
    <PageSection noPadding={false} className={'column-permission-section'}>
      {columnPermissionItems.map(columnPermissionItem => (
        <ColumnPermissionItem
          key={columnPermissionItem}
          index={columnPermissionItem}
          removeRow={removeRow}
          i18nSelectColumn={props.i18nSelectColumn}
          i18nUserRole={props.i18nUserRole}
          i18nRemoveRow={props.i18nRemoveRow}
          i18nSelectRole={props.i18nSelectRole}
          i18nPermission={props.i18nPermission}
          i18nColumn={props.i18nColumn}
        />
      ))}
      <Button variant="link" icon={<PlusCircleIcon />} onClick={addrow}>
        {props.i18nAddPermission}
      </Button>
    </PageSection>
  );
};
