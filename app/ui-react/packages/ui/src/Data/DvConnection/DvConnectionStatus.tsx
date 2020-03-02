import {
  Button,
  ButtonVariant,
  Popover,
  Spinner,
  Split,
  SplitItem,
  Title,
} from '@patternfly/react-core';
import { ErrorCircleOIcon, OkIcon, WarningTriangleIcon } from '@patternfly/react-icons';
import { global_danger_color_100, global_success_color_100, global_warning_color_100 } from '@patternfly/react-tokens';
import * as React from 'react';
import { ConnectionStatus } from '../DvConnection/DvConnectionCard';
import './DvConnectionStatus.css';

export interface IDvConnectionStatusProps {
  dvStatus: string;
  dvStatusMessage?: string;
  i18nRefreshInProgress: string;
  i18nStatusErrorPopoverTitle: string;
  i18nStatusErrorPopoverLink: string;
  loading: boolean;
}

export const DvConnectionStatus: React.FunctionComponent<IDvConnectionStatusProps> = props => {
  if (!props.loading) {
    if (props.dvStatus === ConnectionStatus.FAILED) {
      return (
        <>
          <ErrorCircleOIcon
            className={'dv-connection-status-icon'}
            size={'md'}
            color={global_danger_color_100.value}
          />
          <Popover
            headerContent={<div>{props.i18nStatusErrorPopoverTitle}</div>}
            bodyContent={<div>{props.dvStatusMessage}</div>}
          >
            <Button variant={ButtonVariant.link}>
              {props.i18nStatusErrorPopoverLink}
            </Button>
          </Popover>
        </>
      );
    } else if (props.dvStatus === ConnectionStatus.INACTIVE) {
      return (
        <>
          <WarningTriangleIcon
            className={'dv-connection-status-icon'}
            size={'md'}
            color={global_warning_color_100.value}
          />
          <Popover
            headerContent={<div>{props.i18nStatusErrorPopoverTitle}</div>}
            bodyContent={<div>{props.dvStatusMessage}</div>}
          >
            <Button variant={ButtonVariant.link}>
              {props.i18nStatusErrorPopoverLink}
            </Button>
          </Popover>
        </>
      );
    }
  } else if (props.dvStatus !== ConnectionStatus.ACTIVE) {
    return (
      <Split>
        <SplitItem className={'dv-connection-status-spinner'} ><Spinner size={'md'} /></SplitItem>
        <SplitItem><Title size="md">{props.i18nRefreshInProgress}</Title></SplitItem>
      </Split>
    );
  }
  return (
    <OkIcon
      className={'dv-connection-status-icon'}
      size={'md'}
      color={global_success_color_100.value}
    />
  );
};
