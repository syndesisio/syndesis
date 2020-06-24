import {
  Checkbox,
  Flex,
  FlexItem,
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
import './ColumnPermissionItem.css';

interface IColumnPermissionItemProps {
  i18nSelectColumn: string;
  i18nUserRole: string;
  i18nRemoveRow: string;
  i18nSelectRole: string;
  i18nPermission: string;
  i18nColumn: string;
  index: number;
  removeRow: (index: number) => void;
}
export const ColumnPermissionItem: React.FunctionComponent<IColumnPermissionItemProps> = props => {
  const [columnSelected, setColumnSelected] = React.useState<any>(null);
  const [isColumnOpen, setIsColumnOpen] = React.useState<boolean>(false);

  const [roleSelected, setRoleSelected] = React.useState<any>(null);
  const [isRoleOpen, setIsRoleOpen] = React.useState<boolean>(false);

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

  const onRoleSelect = (event: any, selection: any, isPlaceholder: any) => {
    if (isPlaceholder) {
      clearRoleSelection();
    } else {
      setIsRoleOpen(false);
      setRoleSelected(selection);
    }
  };

  const clearRoleSelection = () => {
    setIsRoleOpen(false);
    setRoleSelected(null);
  };

  const onRoleToggle = (isExpanded: boolean) => {
    setIsRoleOpen(isExpanded);
  };

  const removeRow = () => {
    props.removeRow(props.index);
  };

  const columnOptions = [
    { value: 'nation.NationKey' },
    { value: 'nation.NationCode' },
    { value: 'nation.NationName' },
  ];

  const columnRole = [
    { value: 'Any Authenticated' },
    { value: 'Developer' },
    { value: 'Admin' },
    { value: 'Ux' },
  ];

  const titleIdColumn = 'title-id-Column';
  const titleIdRole = 'title-id-role';

  return (
    <Grid
      //   hasGutter={true}  Use this once pf4 version get updated.
      className={'column-permission-item-row_item'}
    >
      <GridItem span={5}>
        <Text
          component={TextVariants.h3}
          className={'column-permission-item-label'}
        >
          {props.i18nColumn}
        </Text>
        <span id={titleIdColumn} hidden={true}>
          {props.i18nSelectColumn}
        </span>
        <Select
          variant={SelectVariant.single}
          ariaLabelTypeAhead="Select column"
          onToggle={onColumnToggle}
          onSelect={onColumnSelect}
          selections={columnSelected}
          isExpanded={isColumnOpen}
          ariaLabelledBy={titleIdColumn}
          placeholderText={props.i18nSelectColumn}
          className={'column-permission-item-column_select'}
        >
          {columnOptions.map((option, index) => (
            <SelectOption key={index} value={option.value} />
          ))}
        </Select>
      </GridItem>
      <GridItem span={5}>
        <Text
          component={TextVariants.h3}
          className={'column-permission-item-label'}
        >
          {props.i18nUserRole}
        </Text>
        <span id={titleIdRole} hidden={true}>
          {props.i18nSelectRole}
        </span>
        <Select
          variant={SelectVariant.single}
          ariaLabelTypeAhead={props.i18nSelectRole}
          onToggle={onRoleToggle}
          onSelect={onRoleSelect}
          selections={roleSelected}
          isExpanded={isRoleOpen}
          ariaLabelledBy={titleIdRole}
          placeholderText={props.i18nSelectRole}
        >
          {columnRole.map((option, index) => (
            <SelectOption key={index} value={option.value} />
          ))}
        </Select>
      </GridItem>
      <GridItem span={2} className={'column-permission-item-remove'}>
        <Tooltip
          position={TooltipPosition.top}
          content={<div>{props.i18nRemoveRow}</div>}
        >
          <MinusCircleIcon
            onClick={removeRow}
            className={'column-permission-item-remove_icon'}
          />
        </Tooltip>
      </GridItem>
      <GridItem span={10} className={'column-permission-item-permissions'}>
        <Text
          component={TextVariants.h3}
          className={'column-permission-item-label'}
        >
          {props.i18nPermission}
        </Text>
        <Flex>
          <FlexItem>
            <Checkbox
              label="SELECT"
              aria-label="select checkbox "
              id="check-select"
            />
          </FlexItem>
          <FlexItem>
            <Checkbox
              label="INSERT"
              aria-label="insert checkbox "
              id="check-insert"
            />
          </FlexItem>
          <FlexItem>
            <Checkbox
              label="UPDATE"
              aria-label="update checkbox "
              id="check-update"
            />
          </FlexItem>
          <FlexItem>
            <Checkbox
              label="DELETE"
              aria-label="delete checkbox "
              id="check-delete"
            />
          </FlexItem>
        </Flex>
      </GridItem>
    </Grid>
  );
};
