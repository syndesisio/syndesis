import { Label } from 'patternfly-react';
import * as React from 'react';
import { ProgressWithLink } from '../../Shared';
import './PublishStatusWithProgress.css';

export interface IPublishStatusWithProgressProps {
  isProgressWithLink: boolean;
  i18nPublishState: string;
  i18nPublishLogUrlText: string;
  labelType: 'danger' | 'primary' | 'default';
  modified: boolean;
  publishingCurrentStep?: number;
  publishingLogUrl?: string;
  publishingTotalSteps?: number;
  publishingStepText?: string;
  publishVersion?: number;
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
        {props.publishVersion
          ? '(version: ' + props.publishVersion + ') '
          : null}
        {props.modified ? ' M ' : null}
      </div>
    );
  }

  // no progress bar needed so just show a label
  return (
    <React.Fragment>
      <Label
        className={'publish-status-with-progress__label'}
        type={props.labelType}
      >
        {props.i18nPublishState}
      </Label>
      {props.publishVersion ? '(version: ' + props.publishVersion + ') ' : null}
      {props.modified ? ' M ' : null}
    </React.Fragment>
  );
};
