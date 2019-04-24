import { UnrecoverableError } from '@syndesis/ui';
import * as React from 'react';
import { Translation } from 'react-i18next';

export interface IApiErrorProps {
  error?: Error;
  errorInfo?: React.ErrorInfo;
}

export const ApiError: React.SFC<IApiErrorProps> = props => (
  <Translation ns={['shared']}>
    {t => (
      <UnrecoverableError
        i18nTitle={t('error.title')}
        i18nInfo={'error.info'}
        i18nHelp={'error.help'}
        i18nRefreshLabel={'error.refreshButton'}
        i18nReportIssue={'error.reportIssueButton'}
        error={props.error}
        errorInfo={props.errorInfo}
      />
    )}
  </Translation>
);
