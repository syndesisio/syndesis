import {
  Button,
  Grid,
  GridItem,
  InputGroup,
  Select,
  SelectOption,
  SelectVariant,
  Text,
  TextArea,
  TextVariants,
  Tooltip,
  TooltipPosition,
} from '@patternfly/react-core';
import { MinusCircleIcon } from '@patternfly/react-icons';
import React from 'react';
import './RowFilteringItem.css';

// tslint:disable-next-line: no-empty-interface
interface IRowFilteringItemProps {
  i18nCondition: string;
  i18nOperation: string;
  i18nSelectOperation: string;
  i18nUserRole: string;
  i18nValidate: string;
  i18nRemoveRow: string;
  i18nSelectRole:string;
  index: number;
  removeRow: (index: number) => void;
}
export const RowFilteringItem: React.FunctionComponent<IRowFilteringItemProps> = props => {
  const [operationSelected, setOperationSelected] = React.useState<any>(null);
  const [isOperationOpen, setIsOperationOpen] = React.useState<boolean>(false);

  const [roleSelected, setRoleSelected] = React.useState<any>(null);
  const [isRoleOpen, setIsRoleOpen] = React.useState<boolean>(false);

  const onOperationSelect = (
    event: any,
    selection: any,
    isPlaceholder: any
  ) => {
    if (isPlaceholder) {
      clearOperationSelection();
    } else {
      setIsOperationOpen(false);
      setOperationSelected(selection);
    }
  };

  const clearOperationSelection = () => {
    setIsOperationOpen(false);
    setOperationSelected(null);
  };

  const onOperationToggle = (isExpanded: boolean) => {
    setIsOperationOpen(isExpanded);
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

  const operationOptions = [
    { value: 'SELECT' },
    { value: 'INSERT' },
    { value: 'UPDATE' },
    { value: 'DELETE' },
  ];

  const operationRole = [
    { value: 'Any Authenticated' },
    { value: 'Developer' },
    { value: 'Admin' },
    { value: 'Ux' },
  ];

  const titleIdOperator = 'title-id-operator';
  const titleIdRole = 'title-id-role';

  return (
    <Grid
      //   hasGutter={true}  Use this once pf4 version get updated.
      className={'row-filtering-item-row_item'}
    >
      <GridItem span={5}>
        <Text
          component={TextVariants.h3}
          className={'row-filtering-item-label'}
        >
          {props.i18nOperation}
        </Text>
        <span id={titleIdOperator} hidden={true}>
          {props.i18nSelectOperation}
        </span>
        <Select
          variant={SelectVariant.single}
          ariaLabelTypeAhead="Select operation"
          onToggle={onOperationToggle}
          onSelect={onOperationSelect}
          selections={operationSelected}
          isExpanded={isOperationOpen}
          ariaLabelledBy={titleIdOperator}
          placeholderText={props.i18nSelectOperation}
          className={'row-filtering-item-operation_select'}
        >
          {operationOptions.map((option, index) => (
            <SelectOption key={index} value={option.value} />
          ))}
        </Select>
      </GridItem>
      <GridItem span={5}>
        <Text
          component={TextVariants.h3}
          className={'row-filtering-item-label'}
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
          {operationRole.map((option, index) => (
            <SelectOption key={index} value={option.value} />
          ))}
        </Select>
      </GridItem>
      <GridItem span={2} className={'row-filtering-item-remove_icon'}>
      <Tooltip
              position={TooltipPosition.top}
              content={<div>{props.i18nRemoveRow}</div>}
            >
        <MinusCircleIcon onClick={removeRow} />
        </Tooltip>
      </GridItem>
      <GridItem span={10} className={'row-filtering-item-condition'}>
        <Text
          component={TextVariants.h3}
          className={'row-filtering-item-label'}
        >
          {props.i18nCondition}
        </Text>
        <InputGroup>
          <TextArea
            name="textarea2"
            id="textarea2"
            aria-label="textarea with button"
          />
          <Button id="textAreaButton2" variant="control">
            {props.i18nValidate}
          </Button>
        </InputGroup>
      </GridItem>
    </Grid>
  );
};
