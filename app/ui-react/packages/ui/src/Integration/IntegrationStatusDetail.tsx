import { Spinner } from 'patternfly-react';
import * as React from 'react';
import { ProgressWithLink } from '../Shared/ProgressWithLink';
import { IntegrationState, PUBLISHED, UNPUBLISHED } from './models';

import './IntegrationStatusDetail.css';

export interface IIntegrationStatusDetailProps {
  targetState: IntegrationState;
  value?: string;
  currentStep?: number;
  totalSteps?: number;
  logUrl?: string;
  i18nProgressPending: string;
  i18nProgressStarting: string;
  i18nProgressStopping: string;
  i18nLogUrlText: string;
}

export class IntegrationStatusDetail extends React.Component<
  IIntegrationStatusDetailProps
> {
  public render() {
    let fallbackText = this.props.i18nProgressPending;
    switch (this.props.targetState) {
      case PUBLISHED:
        fallbackText = this.props.i18nProgressStarting;
        break;
      case UNPUBLISHED:
        fallbackText = this.props.i18nProgressStopping;
        break;
    }
    return (
      <div
        data-testid={'integration-status-detail'}
        className={'integration-status-detail'}
      >
        {this.props.value && this.props.currentStep && this.props.totalSteps ? (
          <ProgressWithLink
            currentStep={this.props.currentStep}
            totalSteps={this.props.totalSteps}
            value={this.props.value}
            logUrl={this.props.logUrl}
            i18nLogUrlText={this.props.i18nLogUrlText}
          />
        ) : (
          <>
            <Spinner loading={true} inline={true} />
            {fallbackText}
          </>
        )}
      </div>
    );
  }
}
