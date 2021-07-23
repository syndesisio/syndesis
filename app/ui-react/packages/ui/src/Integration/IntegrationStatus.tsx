import { Label } from '@patternfly/react-core';
import classnames from 'classnames';
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
    let color: 'blue' | 'cyan' | 'green' | 'orange' | 'purple' | 'red' | 'grey' = 'grey';
    switch (this.props.currentState) {
      case PUBLISHED:
        label = this.props.i18nPublished;
        color = 'blue';
        break;
      case UNPUBLISHED:
        label = this.props.i18nUnpublished;
        break;
      case ERROR:
        label = this.props.i18nError;
        color = 'red';
        break;
    }
    return (
      <Label
        data-testid={'integration-status-status-label'}
        color={color}
        className={classnames('', labelType, this.props.className)}
      >
        {label}
      </Label>
    );
  }
}
