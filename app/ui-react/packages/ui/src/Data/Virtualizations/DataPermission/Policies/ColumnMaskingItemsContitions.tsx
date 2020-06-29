import {
  Button,
  Grid,
  GridItem,
  InputGroup,
  PageSection,
  Select,
  SelectOption,
  SelectVariant,
  Text,
  TextArea,
  TextInput,
  TextVariants,
  Tooltip,
  TooltipPosition,
} from '@patternfly/react-core';
import { OutlinedTrashAltIcon } from '@patternfly/react-icons';
import React from 'react';
import './ColumnMaskingItemsContitions.css';

interface IColumnMaskingItemsContitionsProps {
  i18nDeleteCondition: string;
  i18nMaskingLabel: string;
  i18nOrder: string;
  i18nUserRole: string;
  i18nSelectRole: string;
  i18nCondition: string;
  i18nValidate: string;
  index: number;
  order: number;
  removeConditionRow: (index: number) => void;
}

export const ColumnMaskingItemsContitions: React.FunctionComponent<IColumnMaskingItemsContitionsProps> = props => {
  const [roleSelected, setRoleSelected] = React.useState<any>(null);
  const [isRoleOpen, setIsRoleOpen] = React.useState<boolean>(false);

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

  const removeConditionRow = () => {
    props.removeConditionRow(props.index);
  };

  const columnRole = [
    { value: 'Any Authenticated' },
    { value: 'Developer' },
    { value: 'Admin' },
    { value: 'Ux' },
  ];

  const titleIdRole = 'title-id-userRole';
  return (
    <PageSection className={'column-masking-item-condition_section'}>
      <Grid>
        <GridItem span={10}>
          <GridItem span={12}>
            <Grid>
              <GridItem
                span={3}
                className={'column-masking-item-condition-order'}
              >
                <Text
                  component={TextVariants.h3}
                  className={'column-masking-item-label'}
                >
                  {props.i18nOrder}
                </Text>
                <TextInput
                  type="text"
                  aria-label="condition order"
                  isDisabled={true}
                  value={props.order}
                />
              </GridItem>
              <GridItem span={9}>
                <Text
                  component={TextVariants.h3}
                  className={'column-masking-item-label'}
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
            </Grid>
          </GridItem>
          <GridItem span={12} className={'column-masking-item-element_rows'}>
            <Text
              component={TextVariants.h3}
              className={'column-masking-item-label'}
            >
              {props.i18nMaskingLabel}
            </Text>
            <InputGroup>
              <TextArea
                name="columnMasking-text"
                id="columnMasking-text"
                aria-label="mask textarea with button"
              />
            </InputGroup>
          </GridItem>
          <GridItem span={12} className={'column-masking-item-element_rows'}>
            <Text
              component={TextVariants.h3}
              className={'column-masking-item-label'}
            >
              {props.i18nCondition}
            </Text>
            <InputGroup>
              <TextArea
                name="columnMasking-text-condition"
                id="columnMasking-text-condition"
                aria-label="condition textarea with button"
              />
              <Button id="validateConditionButton" variant="control">
                {props.i18nValidate}
              </Button>
            </InputGroup>
          </GridItem>
        </GridItem>
        <GridItem span={2} className={'column-permission-item-remove'}>
          <Tooltip
            position={TooltipPosition.top}
            content={<div>{props.i18nDeleteCondition}</div>}
          >
            <OutlinedTrashAltIcon
              onClick={removeConditionRow}
              className={'column-permission-item-condition-delete'}
            />
          </Tooltip>
        </GridItem>
      </Grid>
    </PageSection>
  );
};
