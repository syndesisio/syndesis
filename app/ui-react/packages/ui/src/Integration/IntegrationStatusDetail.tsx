import { Spinner } from '@patternfly/react-core';
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

export const IntegrationStatusDetail: React.FunctionComponent<IIntegrationStatusDetailProps> = ({
  targetState,
  value,
  currentStep,
  totalSteps,
  logUrl,
  i18nProgressPending,
  i18nProgressStarting,
  i18nProgressStopping,
  i18nLogUrlText,
}) => {
  let fallbackText = i18nProgressPending;
  switch (targetState) {
    case PUBLISHED:
      fallbackText = i18nProgressStarting;
      break;
    case UNPUBLISHED:
      fallbackText = i18nProgressStopping;
      break;
  }
  return (
    <div
      data-testid={'integration-status-detail'}
      className={'integration-status-detail'}
    >
      {value && currentStep && totalSteps ? (
        <ProgressWithLink
          currentStep={currentStep}
          totalSteps={totalSteps}
          value={value}
          logUrl={logUrl}
          i18nLogUrlText={i18nLogUrlText}
        />
      ) : (
        <>
          <Spinner size={'lg'} />&nbsp;{fallbackText}
        </>
      )}
    </div>
  );
};
