import { Badge } from '@patternfly/react-core';
import { OkIcon } from '@patternfly/react-icons';
import { Label } from 'patternfly-react';
import * as React from 'react';
import { ProgressWithLink } from '../../Shared';
import './PublishStatusWithProgress.css';

export interface IPublishStatusWithProgressProps {
  isProgressWithLink: boolean;
  inListView: boolean;
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
        <span className={'publish-status-with-progress_text'}>
          {props.publishVersion && ` (Version ${props.publishVersion}) `}
        </span>
        {props.modified && (
          <Badge key={1} className={'publish-status-with-progress-badge'}>
            Modified
          </Badge>
        )}
      </div>
    );
  }
  if (props.inListView) {
    return (
      <React.Fragment>
        <Label
          className={'publish-status-with-progress_Label'}
          type={props.labelType}
        >
          {props.i18nPublishState}
        </Label>
        {props.publishVersion ? (
          <span className={'publish-status-with-progress_text'}>
            {' (Version' + props.publishVersion + ') '}
          </span>
        ) : (
          // tslint:disable-next-line: jsx-self-close
          <span className={'publish-status-with-progress_version-container'} />
        )}
        {props.modified ? (
          <Badge key={2} className={'publish-status-with-progress-badge'}>
            Modified
          </Badge>
        ) : (
          // tslint:disable-next-line: jsx-self-close
          <span className={'publish-status-with-progress-badge-container'} />
        )}
      </React.Fragment>
    );
  }

  // no progress bar needed so just show a label
  return (
    <React.Fragment>
      {props.i18nPublishState === 'Running' ? (
        <OkIcon
          color={'#49B720'}
          height={'1.25rem'}
          width={'1.25rem'}
          className={'publish-status-with-progress-margin10'}
        />
      ) : (
        ''
      )}
      <span className={'publish-status-with-progress_text'}>
        {props.i18nPublishState}
        {props.publishVersion
          ? ' (Version' + props.publishVersion + ') '
          : null}
      </span>
      {props.modified ? (
        <Badge key={2} className={'publish-status-with-progress-badge'}>
          Modified
        </Badge>
      ) : null}
    </React.Fragment>
  );
};
