import { Label } from '@patternfly/react-core';
import { global_active_color_100, global_danger_color_100, global_default_color_100 } from '@patternfly/react-tokens';
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
    let style:any = { background: global_default_color_100.value };
    switch (this.props.currentState) {
      case PUBLISHED:
        label = this.props.i18nPublished;
        style = { background: global_active_color_100.value };
        break;
      case UNPUBLISHED:
        label = this.props.i18nUnpublished;
        break;
      case ERROR:
        label = this.props.i18nError;
        style = { background: global_danger_color_100 };
        break;
    }
    return (
      <Label
        data-testid={'integration-status-status-label'}
        style={style}
        className={classnames('', labelType, this.props.className)}
      >
        {label}
      </Label>
    );
  }
}
