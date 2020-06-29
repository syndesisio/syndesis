import {
  Button,
  Expandable,
  Grid,
  GridItem,
  Select,
  SelectOption,
  SelectVariant,
  Text,
  TextVariants,
  Tooltip,
  TooltipPosition,
} from '@patternfly/react-core';
import { MinusCircleIcon } from '@patternfly/react-icons';
import React from 'react';
import { ColumnMaskingItemsContitions } from '..';
import './ColumnMaskingItems.css';

interface IColumnMaskingItemsProps {
  i18nAddACondition: string;
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
  i18nValidate: string;
  i18nDelete: string;
  index: number;
  order: number;
  removeRow: (index: number) => void;
}

const getRandomId = () => {
  return Math.floor(Math.random() * 100000);
};

export const ColumnMaskingItems: React.FunctionComponent<IColumnMaskingItemsProps> = props => {
  const [columnMaskingConditions, setColumnMaskingConditions] = React.useState<
    number[]
  >([0]);

  const [columnSelected, setColumnSelected] = React.useState<any>(null);
  const [isColumnOpen, setIsColumnOpen] = React.useState<boolean>(false);

  const onColumnSelect = (event: any, selection: any, isPlaceholder: any) => {
    if (isPlaceholder) {
      clearColumnSelection();
    } else {
      setIsColumnOpen(false);
      setColumnSelected(selection);
    }
  };

  const clearColumnSelection = () => {
    setIsColumnOpen(false);
    setColumnSelected(null);
  };

  const onColumnToggle = (isExpanded: boolean) => {
    setIsColumnOpen(isExpanded);
  };

  const addConditionrow = () => {
    const columnMaskingConditionsCopy = [...columnMaskingConditions];
    const rowId = getRandomId();
    columnMaskingConditionsCopy.push(rowId);
    setColumnMaskingConditions(columnMaskingConditionsCopy);
  };

  const removeConditionRow = (index: number) => {
    const columnMaskingConditionsCopy = [...columnMaskingConditions];
    const indexRemove = columnMaskingConditionsCopy.indexOf(index);
    columnMaskingConditionsCopy.splice(indexRemove, 1);
    setColumnMaskingConditions(columnMaskingConditionsCopy);
  };

  const removeRow = () => {
    props.removeRow(props.index);
  };

  const columnOptions = [
    { value: 'nation.NationKey' },
    { value: 'nation.NationCode' },
    { value: 'nation.NationName' },
  ];

  const titleIdColumn = 'title-id-maskingColumn';

  return (
    <>
      <Grid
        //   hasGutter={true}  Use this once pf4 version get updated.
        className={'column-masking-item-row_item'}
      >
        <GridItem span={10}>
          <Expandable toggleText={`Column mask ${props.order}`}>
            <Grid>
              <GridItem span={12}>
                <Text
                  component={TextVariants.h3}
                  className={'column-masking-item-label'}
                >
                  {props.i18nColumn}
                </Text>
                <span id={titleIdColumn} hidden={true}>
                  {props.i18nSelectColumn}
                </span>
                <Select
                  variant={SelectVariant.single}
                  ariaLabelTypeAhead={props.i18nSelectColumn}
                  onToggle={onColumnToggle}
                  onSelect={onColumnSelect}
                  selections={columnSelected}
                  isExpanded={isColumnOpen}
                  ariaLabelledBy={titleIdColumn}
                  placeholderText={props.i18nSelectColumn}
                >
                  {columnOptions.map((option, index) => (
                    <SelectOption key={index} value={option.value} />
                  ))}
                </Select>
              </GridItem>
              <GridItem
                span={12}
                className={'column-masking-item-element_rows'}
              >
                <Text
                  component={TextVariants.h3}
                  className={'column-masking-item-label'}
                >
                  {props.i18nMaskings}
                </Text>
              </GridItem>
              {columnMaskingConditions.map((columnMaskingCondition, index) => (
                <ColumnMaskingItemsContitions
                  key={columnMaskingCondition + index}
                  i18nDeleteCondition={props.i18nDeleteCondition}
                  i18nMaskingLabel={props.i18nMaskingLabel}
                  i18nOrder={props.i18nOrder}
                  i18nUserRole={props.i18nUserRole}
                  i18nSelectRole={props.i18nSelectRole}
                  i18nCondition={props.i18nCondition}
                  i18nValidate={props.i18nValidate}
                  index={columnMaskingCondition}
                  order={index}
                  removeConditionRow={removeConditionRow}
                />
              ))}
            </Grid>
            <Button
              variant="link"
              onClick={addConditionrow}
              className={'column-masking-tab-add_button'}
            >
              {props.i18nAddACondition}
            </Button>
          </Expandable>
        </GridItem>
        <GridItem span={2} className={'column-permission-item-remove'}>
          <Tooltip
            position={TooltipPosition.top}
            content={<div>{props.i18nRemoveRow}</div>}
          >
            <Button
              variant="link"
              onClick={removeRow}
              icon={<MinusCircleIcon />}
            >
              {props.i18nDelete}
            </Button>
          </Tooltip>
        </GridItem>
      </Grid>
    </>
  );
};
