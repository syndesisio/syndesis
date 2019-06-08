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

export class VirtualizationPublishStatus extends React.Component<
  IVirtualizationPublishStatusProps
> {
  public render() {
    const labelType =
      this.props.currentState === FAILED
        ? 'danger'
        : this.props.currentState === RUNNING
        ? 'primary'
        : 'default';
    let label = '';
    let inProgressMsg = '';
    switch (this.props.currentState) {
      case RUNNING:
        label = this.props.i18nPublished;
        break;
      case FAILED:
        label = this.props.i18nError;
        break;
      case NOTFOUND:
        label = this.props.i18nUnpublished;
        break;
      case SUBMITTED:
        inProgressMsg = this.props.i18nPublishInProgress;
        break;
      case CANCELLED:
      case DELETE_SUBMITTED:
      case DELETE_REQUEUE:
      case DELETE_DONE:
        inProgressMsg = this.props.i18nUnpublishInProgress;
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
}
