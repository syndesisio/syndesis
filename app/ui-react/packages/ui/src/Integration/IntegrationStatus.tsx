import classnames from 'classnames';
import { Label } from 'patternfly-react';
import * as React from 'react';
import {
  ERROR,
  IntegrationState,
  PENDING,
  PUBLISHED,
  UNPUBLISHED,
} from './models';

export interface IIntegrationStatusProps {
  currentState?: IntegrationState;
  i18nPublished: string;
  i18nUnpublished: string;
  i18nError: string;
  className?: string;
}

export class IntegrationStatus extends React.Component<
  IIntegrationStatusProps
> {
  public render() {
    const labelType =
      this.props.currentState === ERROR
        ? 'danger'
        : this.props.currentState === PUBLISHED ||
          this.props.currentState === PENDING
        ? 'primary'
        : 'default';
    let label = PENDING; // it's a parachute
    switch (this.props.currentState) {
      case PUBLISHED:
        label = this.props.i18nPublished;
        break;
      case UNPUBLISHED:
        label = this.props.i18nUnpublished;
        break;
      case ERROR:
        label = this.props.i18nError;
        break;
    }
    return (
      <Label
        data-testid={'integration-status-status-label'}
        className={classnames('', this.props.className)}
        type={labelType}>
        {label}
      </Label>
    );
  }
}
