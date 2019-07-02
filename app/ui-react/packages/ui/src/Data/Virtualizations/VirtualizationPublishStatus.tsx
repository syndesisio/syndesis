import { Label, Spinner } from 'patternfly-react';
import * as React from 'react';
import {
  BUILDING,
  CANCELLED,
  CONFIGURING,
  DELETE_DONE,
  DELETE_REQUEUE,
  DELETE_SUBMITTED,
  DEPLOYING,
  FAILED,
  NOTFOUND,
  RUNNING,
  SUBMITTED,
  VirtualizationPublishState,
} from './models';
import './VirtualizationPublishStatus.css';

export interface IVirtualizationPublishStatusProps {
  currentState?: VirtualizationPublishState;
  i18nPublished: string;
  i18nUnpublished: string;
  i18nPublishInProgress: string;
  i18nUnpublishInProgress: string;
  i18nError: string;
}

export const VirtualizationPublishStatus: React.FunctionComponent<
  IVirtualizationPublishStatusProps
> = props => {

  const labelType =
    props.currentState === FAILED
      ? 'danger'
      : props.currentState === RUNNING
        ? 'primary'
        : 'default';
  let label = '';
  let inProgressMsg = '';
  switch (props.currentState) {
    case RUNNING:
      label = props.i18nPublished;
      break;
    case FAILED:
      label = props.i18nError;
      break;
    case NOTFOUND:
      label = props.i18nUnpublished;
      break;
    case SUBMITTED:
      inProgressMsg = props.i18nPublishInProgress;
      break;
    case CANCELLED:
    case DELETE_SUBMITTED:
    case DELETE_REQUEUE:
    case DELETE_DONE:
      inProgressMsg = props.i18nUnpublishInProgress;
      break;
    case CONFIGURING:
    case BUILDING:
    case DEPLOYING:
      label = DEPLOYING;
      break;
  }

  return (
    <>
      {inProgressMsg.length > 0 ? (
        <>
          <Spinner loading={true} inline={true} />
          {inProgressMsg}&nbsp;&nbsp;
          </>
      ) : (
          <Label
            className={'virtualization-publish-status-label'}
            type={labelType}
          >
            {label}
          </Label>
        )}
    </>
  );
}
