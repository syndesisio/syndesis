import { Label } from 'patternfly-react';
import * as React from 'react';
import { ProgressWithLink } from '../../Shared';
import './PublishStatusWithProgress.css';

export interface IPublishStatusWithProgressProps {
  isProgressWithLink: boolean;
  i18nPublishState: string;
  labelType: 'danger' | 'primary' | 'default';
  i18nPublishLogUrlText: string;
  publishingCurrentStep?: number;
  publishingLogUrl?: string;
  publishingTotalSteps?: number;
  publishingStepText?: string;
}

export const PublishStatusWithProgress: React.FunctionComponent<
  IPublishStatusWithProgressProps
> = props => {
  if (props.isProgressWithLink) {
    return (
      <div
        data-testid={'publish-status-with-progress-progress'}
        className={'publish-status-with-progress-progress'}
      >
        <ProgressWithLink
          logUrl={props.publishingLogUrl}
          value={props.publishingStepText ? props.publishingStepText : ''}
          currentStep={
            props.publishingCurrentStep ? props.publishingCurrentStep : 0
          }
          totalSteps={
            props.publishingTotalSteps ? props.publishingTotalSteps : 4
          }
          i18nLogUrlText={props.i18nPublishLogUrlText}
        />
      </div>
    );
  }

  // no progress bar needed so just show a label
  return (
    <Label
      className={'publish-status-with-progress__label'}
      type={props.labelType}
    >
      {props.i18nPublishState}
    </Label>
  );
};
