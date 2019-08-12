import {
  Button,
  ButtonVariant,
  Chip,
  Dropdown,
  DropdownItem,
  DropdownToggle,
  InputGroup,
  Modal,
  Split,
  SplitItem,
  Stack,
  StackItem,
  Text,
  TextContent,
  TextInput,
  TextList,
  TextListItem,
  TextListItemVariants,
  TextListVariants,
  TextVariants,
  Toolbar,
  ToolbarGroup,
  ToolbarItem,
} from '@patternfly/react-core';
import {
  ArrowDownIcon,
  ArrowUpIcon,
  SearchIcon,
} from '@patternfly/react-icons';
import * as React from 'react';
import { toValidHtmlId } from '../../../helpers';
import './ViewOutputToolbar.css';

export interface IViewOutputToolbarProps {
  a11yFilterColumns: string;
  a11yFilterText: string;
  a11yReorderDown: string;
  a11yReorderUp: string;
  activeFilter?: string;
  columnsToDelete?: string[];
  enableAddColumn: boolean;
  enableRemoveColumn: boolean;
  enableReorderColumnDown: boolean;
  enableReorderColumnUp: boolean;
  enableSave: boolean;
  i18nActiveFilter: string;
  i18nAddColumn: string;
  i18nCancel: string;
  i18nFilterPlaceholder: string;
  i18nFilterResultsMessage?: string;
  i18nFilterValues: string[];
  i18nRemove: string;
  i18nRemoveColumn: string;
  i18nRemoveColumnDialogConfirmMessage: string;
  i18nRemoveColumnDialogHeader: string;
  i18nRemoveColumnDialogMessage: string;
  i18nSave: string;
  onActiveFilterClosed(): void;
  onAddColumn(): void;
  onCancel(): void;
  onFilter(filterBy: string, filter: string): void;
  onRemoveColumn(): void;
  onReorderColumnDown(): void;
  onReorderColumnUp(): void;
  onSave(): void;
}

export const ViewOutputToolbar: React.FunctionComponent<
  IViewOutputToolbarProps
> = props => {
  const [isRemoveColumnDialogOpen, setRemoveColumnDialogOpen] = React.useState(
    false
  );

  // indicates if filter by dropdown is open
  const [isFilterOpen, setFilterOpen] = React.useState(false);

  // the filter text
  const [filter, setFilter] = React.useState();

  // the selected filter by
  const [filterBy, setFilterBy] = React.useState(props.i18nFilterValues[0]);

  const handleCancelRemoveColumn = () => {
    toggleRemoveColumnDialogOpen();
    props.onCancel();
  };

  const doFilter = () => {
    props.onFilter(filterBy, filter);
  };

  const handleFilterChange = (newFilter: string) => {
    setFilter(newFilter);
  };

  const handleRemoveColumn = () => {
    toggleRemoveColumnDialogOpen();
    props.onRemoveColumn();
  };

  const handleFilterByChanged = (
    event: React.SyntheticEvent<HTMLDivElement, Event>
  ) => {
    setFilterBy(event.currentTarget.textContent || props.i18nFilterValues[0]);
    toggleFilterByDropdown();
  };

  const toggleFilterByDropdown = () => {
    setFilterOpen(!isFilterOpen);
  };

  const toggleRemoveColumnDialogOpen = () => {
    // don't open if there are no columns to delete
    if (
      !isRemoveColumnDialogOpen &&
      (!props.columnsToDelete || props.columnsToDelete.length === 0)
    ) {
      return;
    }

    setRemoveColumnDialogOpen(!isRemoveColumnDialogOpen);
  };

  return (
    <>
      <Modal
        actions={[
          <Button
            key="cancel"
            onClick={handleCancelRemoveColumn}
            variant={ButtonVariant.secondary}
          >
            {props.i18nCancel}
          </Button>,
          <Button
            isDisabled={
              !props.columnsToDelete || props.columnsToDelete.length === 0
            }
            key="remove"
            onClick={handleRemoveColumn}
            variant={ButtonVariant.primary}
          >
            {props.i18nRemove}
          </Button>,
        ]}
        isOpen={isRemoveColumnDialogOpen}
        isSmall={true}
        onClose={toggleRemoveColumnDialogOpen}
        title={props.i18nRemoveColumnDialogHeader}
      >
        {props.columnsToDelete && props.columnsToDelete.length !== 0 ? (
          <Stack>
            <StackItem isFilled={false}>
              <TextContent>
                <Text
                  className={'view-output-toolbar__removeColumnDialog-message'}
                  component={TextVariants.p}
                >
                  {props.i18nRemoveColumnDialogMessage}
                </Text>
              </TextContent>
            </StackItem>
            <StackItem
              isFilled={true}
              style={{ maxHeight: '200px', overflowY: 'scroll' }}
            >
              <TextContent>
                <TextList
                  className={
                    'view-output-toolbar__removeColumnDialog-columnList'
                  }
                  component={TextListVariants.dl}
                >
                  {props.columnsToDelete.map(
                    (column: string, index: number) => {
                      return (
                        <TextListItem
                          component={TextListItemVariants.dt}
                          key={column}
                          style={{
                            gridColumn: index === 0 || index % 2 === 0 ? 1 : 2,
                          }}
                        >
                          {column}
                        </TextListItem>
                      );
                    }
                  )}
                </TextList>
              </TextContent>
            </StackItem>
            <StackItem isFilled={false}>
              <TextContent>
                <Text component={TextVariants.p}>
                  {props.i18nRemoveColumnDialogConfirmMessage}
                </Text>
              </TextContent>
            </StackItem>
          </Stack>
        ) : null}
      </Modal>
      <Stack>
        <StackItem
          className={'view-output-toolbar__stackItem'}
          isFilled={false}
        >
          <Toolbar>
            <ToolbarGroup>
              <ToolbarItem>
                <Dropdown
                  data-testid={'view-output-toolbar-filter-dropdown'}
                  onSelect={handleFilterByChanged}
                  toggle={
                    <DropdownToggle
                      data-testid={'view-output-toolbar-filter-toggle'}
                      onToggle={toggleFilterByDropdown}
                    >
                      {filterBy}
                    </DropdownToggle>
                  }
                  isOpen={isFilterOpen}
                  dropdownItems={props.i18nFilterValues.map(
                    (filterType: string, index: number) => {
                      return (
                        <DropdownItem
                          component={'button'}
                          data-testid={`view-output-toolbar-${toValidHtmlId(
                            filterType
                          )}-filter`}
                          key={index}
                        >
                          {filterType}
                        </DropdownItem>
                      );
                    }
                  )}
                />
              </ToolbarItem>
              <ToolbarItem>
                <InputGroup>
                  <TextInput
                    aria-label={props.a11yFilterText}
                    data-testid={'view-output-toolbar-filter-input'}
                    onChange={handleFilterChange}
                    placeholder={props.i18nFilterPlaceholder}
                    type={'search'}
                  />
                  <Button
                    aria-label={props.a11yFilterColumns}
                    data-testid={'view-output-toolbar-filter-button'}
                    isDisabled={!filter || !filterBy}
                    onClick={doFilter}
                    variant={ButtonVariant.tertiary}
                  >
                    <SearchIcon />
                  </Button>
                </InputGroup>
              </ToolbarItem>
            </ToolbarGroup>
            <ToolbarGroup>
              <ToolbarItem>
                <Button
                  data-testid={'view-output-toolbar-add-column-button'}
                  isDisabled={!props.enableAddColumn}
                  onClick={props.onAddColumn}
                  variant={ButtonVariant.secondary}
                >
                  {props.i18nAddColumn}
                </Button>{' '}
              </ToolbarItem>
              <ToolbarItem>
                <Button
                  data-testid={'view-output-toolbar-remove-column-button'}
                  isDisabled={!props.enableRemoveColumn}
                  onClick={toggleRemoveColumnDialogOpen}
                  variant={ButtonVariant.plain}
                >
                  {props.i18nRemoveColumn}
                </Button>
              </ToolbarItem>
            </ToolbarGroup>
            <ToolbarGroup>
              <ToolbarItem>
                <Button
                  aria-label={props.a11yReorderUp}
                  className={
                    'pf-u-p-sm pf-u-mr-sm view-output-toolbar__reorderButton'
                  }
                  data-testid={'view-output-toolbar-reorder-up-button'}
                  isDisabled={!props.enableReorderColumnUp}
                  isInline={true}
                  onClick={props.onReorderColumnUp}
                >
                  <ArrowUpIcon size="sm" />
                </Button>
              </ToolbarItem>
              <ToolbarItem>
                <Button
                  aria-label={props.a11yReorderDown}
                  className={'pf-u-p-sm view-output-toolbar__reorderButton'}
                  data-testid={'view-output-toolbar-reorder-down-button'}
                  isInline={true}
                  isDisabled={!props.enableReorderColumnDown}
                  onClick={props.onReorderColumnDown}
                >
                  <ArrowDownIcon size="sm" />
                </Button>
              </ToolbarItem>
            </ToolbarGroup>
            <ToolbarGroup className={'pf-m-align-right'}>
              <ToolbarItem>
                <Button
                  data-testid={'view-output-toolbar-save-button'}
                  isDisabled={!props.enableSave}
                  onClick={props.onSave}
                  variant={ButtonVariant.primary}
                >
                  {props.i18nSave}
                </Button>
              </ToolbarItem>
              <ToolbarItem>
                <Button
                  data-testid={'view-output-toolbar-cancel-button'}
                  onClick={props.onCancel}
                  variant={ButtonVariant.plain}
                >
                  {props.i18nCancel}
                </Button>
              </ToolbarItem>
            </ToolbarGroup>
          </Toolbar>
        </StackItem>
        {props.i18nFilterResultsMessage && props.activeFilter ? (
          <StackItem
            className={'view-output-toolbar__stackItem'}
            isFilled={false}
          >
            <Split gutter="md">
              <SplitItem isFilled={false} style={{ alignSelf: 'center' }}>
                <TextContent>
                  <Text className={'view-output-toolbar__filterResultsMessage'}>
                    {props.i18nFilterResultsMessage}
                  </Text>
                </TextContent>
              </SplitItem>
              <SplitItem isFilled={false} style={{ alignSelf: 'center' }}>
                <TextContent>
                  <Text>{props.i18nActiveFilter}</Text>
                </TextContent>
              </SplitItem>
              <SplitItem isFilled={true}>
                <Chip
                  className={'view-output-toolbar__activeFilter'}
                  onClick={props.onActiveFilterClosed}
                >
                  {props.activeFilter}
                </Chip>
              </SplitItem>
            </Split>
          </StackItem>
        ) : null}
      </Stack>
    </>
  );
};
