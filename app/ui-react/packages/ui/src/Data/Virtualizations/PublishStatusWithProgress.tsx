import * as React from 'react';
import {
  ProgressWithLink,
} from '../../Shared';
import {
  BUILDING,
  CONFIGURING,
  DEPLOYING,
  VirtualizationPublishState,
} from './models';
import './PublishStatusWithProgress.css';
import { VirtualizationPublishStatus } from './VirtualizationPublishStatus';

export interface IPublishStatusWithProgressProps {
  publishedState?: VirtualizationPublishState;
  i18nError: string;
  i18nPublished: string;
  i18nUnpublished: string;
  i18nPublishInProgress: string;
  i18nUnpublishInProgress: string;
  i18nPublishLogUrlText: string;
  publishingCurrentStep?: number;
  publishingLogUrl?: string;
  publishingTotalSteps?: number;
  publishingStepText?: string;
}

export const PublishStatusWithProgress: React.FunctionComponent<
IPublishStatusWithProgressProps
> = props => {

  const publishInProgress =
    props.publishedState === BUILDING ||
      props.publishedState === CONFIGURING ||
      props.publishedState === DEPLOYING
      ? true
      : false;

  return (
    publishInProgress ? (
      <div
        data-testid={'publish-status-with-progress-progress'}
        className={'publish-status-with-progress-progress'}
      >
        <ProgressWithLink
          logUrl={props.publishingLogUrl}
          value={
            props.publishingStepText
              ? props.publishingStepText
              : ''
          }
          currentStep={
            props.publishingCurrentStep
              ? props.publishingCurrentStep
              : 0
          }
          totalSteps={
            props.publishingTotalSteps
              ? props.publishingTotalSteps
              : 4
          }
          i18nLogUrlText={props.i18nPublishLogUrlText}
        />
      </div>
    ) : (
        <VirtualizationPublishStatus
          currentState={props.publishedState}
          i18nPublished={props.i18nPublished}
          i18nUnpublished={props.i18nUnpublished}
          i18nPublishInProgress={props.i18nPublishInProgress}
          i18nUnpublishInProgress={props.i18nUnpublishInProgress}
          i18nError={props.i18nError}
        />
      )
  );
}
