import { Label } from 'patternfly-react';
import * as React from 'react';
import {
  BUILDING,
  CANCELLED,
  CONFIGURING,
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
    let label = NOTFOUND; // default to not found
    switch (this.props.currentState) {
      case RUNNING:
        label = this.props.i18nPublished;
        break;
      case CANCELLED:
      case NOTFOUND:
        label = this.props.i18nUnpublished;
        break;
      case FAILED:
        label = this.props.i18nError;
        break;
      case SUBMITTED:
      case CONFIGURING:
      case BUILDING:
        label = DEPLOYING;
        break;
    }
    return (
      <Label className={'virtualization-publish-status-label'} type={labelType}>
        {label}
      </Label>
    );
  }
}
