import {
  DataListAction,
  DataListCell,
  DataListContent,
  DataListItem,
  DataListItemCells,
  DataListItemRow,
  DataListToggle,
  Popover,
  Split,
  SplitItem,
  Stack,
  StackItem,
} from '@patternfly/react-core';
import { WarningTriangleIcon } from '@patternfly/react-icons';
import { global_warning_color_100 } from '@patternfly/react-tokens';
import * as React from 'react';
import { toValidHtmlId } from '../../helpers';
import { ButtonLink } from '../../Layout';

import './IntegrationEditorStepsListItem.css';

export interface IIntegrationEditorStepsListItemProps {
  action: string;
  children?: React.ReactNode;
  stepName: string;
  stepDescription: string;
  shape: string;
  showWarning: boolean;
  i18nWarningTitle: React.ReactNode;
  i18nWarningMessage: React.ReactNode;
  actions: any;
  icon: React.ReactNode;
}

export const IntegrationEditorStepsListItem: React.FunctionComponent<IIntegrationEditorStepsListItemProps> =
  ({
    action,
    children,
    stepName,
    stepDescription,
    shape,
    showWarning,
    i18nWarningTitle,
    i18nWarningMessage,
    actions,
    icon,
  }) => {
    const hasExpander = typeof children !== 'undefined';
    const [expanded, setExpanded] = React.useState(hasExpander);
    const [showWarningPopover, setShowWarningPopover] = React.useState(false);
    const toggleWarningPopover = () =>
      setShowWarningPopover(!showWarningPopover);
    const id = toValidHtmlId(stepName);
    return (
      <DataListItem
        aria-labelledby={id}
        data-testid={`integration-editor-steps-list-item-${id}-list-item`}
        isExpanded={expanded}
      >
        <DataListItemRow>
          {hasExpander && (
            <DataListToggle
              onClick={() => setExpanded(!expanded)}
              isExpanded={expanded}
              id={`integration-editor-steps-list-item-${id}-expander`}
            />
          )}
          <DataListItemCells
            dataListCells={[
              <DataListCell
                key={'icon'}
                width={1}
                aria-label={'editor steps list item icon'}
                data-testid={'editor-step-icon'}
              >
                {icon}
              </DataListCell>,
              <DataListCell
                key={0}
                width={3}
                aria-label={'editor steps list item name'}
              >
                <Stack>
                  <StackItem data-testid={'editor-step-name'}>
                    <b id={id}>{stepName}</b>
                  </StackItem>
                  <StackItem data-testid={'editor-step-description'}>
                    {stepDescription}
                  </StackItem>
                </Stack>
              </DataListCell>,
              <DataListCell
                key={2}
                width={3}
                aria-label={'editor steps list item additional info'}
                data-testid={'editor-step-info'}
              >
                <Split>
                  <SplitItem isFilled={true}>
                    <Stack>
                      <StackItem>
                        <strong>Action:</strong>&nbsp;
                        <span data-testid={'editor-step-action'}>{action}</span>
                      </StackItem>
                      <StackItem>
                        <strong>Data Type:</strong>&nbsp;
                        <span data-testid={'editor-step-datashape'}>
                          {shape}
                        </span>
                      </StackItem>
                    </Stack>
                  </SplitItem>
                  <SplitItem>
                    {showWarning && (
                      <Popover
                        position={'auto'}
                        isVisible={showWarningPopover}
                        shouldClose={() => setShowWarningPopover(false)}
                        headerContent={i18nWarningTitle}
                        bodyContent={i18nWarningMessage}
                      >
                        <ButtonLink
                          data-testid={`integration-editor-steps-list-item-${id}-warning-button`}
                          as={'link'}
                          onClick={toggleWarningPopover}
                        >
                          <WarningTriangleIcon
                            color={global_warning_color_100.value}
                            size={'md'}
                          />
                        </ButtonLink>
                      </Popover>
                    )}
                  </SplitItem>
                </Split>
              </DataListCell>,
            ]}
          />
          <DataListAction
            id={`${id}-actions`}
            aria-labelledby={id}
            aria-label={`${stepName} actions`}
          >
            {actions}
          </DataListAction>
        </DataListItemRow>
        <DataListContent
          aria-label={`${stepName} content`}
          id={`${id}-content`}
          isHidden={!expanded}
          hasNoPadding={true}
        >
          <div style={{ margin: '10px' }}>{children}</div>
        </DataListContent>
      </DataListItem>
    );
  };
