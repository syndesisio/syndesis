import { Spinner } from 'patternfly-react';
import * as React from 'react';
import { ProgressWithLink } from '../../Shared';
import './VirtualizationPublishStatusDetail.css';

export interface IVirtualizationPublishStatusDetailProps {
  currentStep?: number;
  totalSteps?: number;
  stepText?: string;
  logUrl?: string;
  i18nPublishInProgress: string;
  i18nLogUrlText: string;
}

export class VirtualizationPublishStatusDetail extends React.Component<
  IVirtualizationPublishStatusDetailProps
> {
  public render() {
    return (
      <div
        data-testid={'virtualization-publish-status-detail'}
        className={'virtualization-publish-status-detail'}
      >
        {this.props.stepText &&
        this.props.currentStep &&
        this.props.totalSteps ? (
          <ProgressWithLink
            currentStep={this.props.currentStep}
            totalSteps={this.props.totalSteps}
            value={this.props.stepText}
            logUrl={this.props.logUrl}
            i18nLogUrlText={this.props.i18nLogUrlText}
          />
        ) : (
          <>
            <Spinner loading={true} inline={true} />
            {this.props.i18nPublishInProgress}
          </>
        )}
      </div>
    );
  }
}
