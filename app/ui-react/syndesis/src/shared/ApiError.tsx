import { UnrecoverableError } from '@syndesis/ui';
import * as React from 'react';
import { Translation } from 'react-i18next';

export interface IApiErrorProps {
  error: Error | string;
  errorInfo?: React.ErrorInfo;
}

export const ApiError: React.SFC<IApiErrorProps> = props => (
  <Translation ns={['shared']}>
    {t => (
      <UnrecoverableError
        i18nTitle={t('error.title')}
        i18nInfo={t('error.info')}
        i18nHelp={t('error.help')}
        i18nRefreshLabel={t('error.refreshButton')}
        i18nReportIssue={t('error.reportIssueButton')}
        i18nShowErrorInfoLabel={t('error.showErrorInfoButton')}
        error={
          typeof props.error === 'string' ? new Error(props.error) : props.error
        }
        errorInfo={props.errorInfo}
      />
    )}
  </Translation>
);
