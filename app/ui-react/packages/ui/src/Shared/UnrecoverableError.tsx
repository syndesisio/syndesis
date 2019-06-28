import { EmptyState } from 'patternfly-react';
import { useState } from 'react';
import * as React from 'react';
import { ButtonLink, Container } from '../Layout';

export interface IUnrecoverableErrorProps {
  i18nTitle: string;
  i18nInfo: string;
  i18nHelp: string;
  i18nRefreshLabel: string;
  i18nReportIssue: string;
  i18nShowErrorInfoLabel?: string;
  error?: Error;
  errorInfo?: React.ErrorInfo;
}

export const UnrecoverableError: React.FC<IUnrecoverableErrorProps> = ({
  i18nTitle,
  i18nInfo,
  i18nHelp,
  i18nRefreshLabel,
  i18nReportIssue,
  i18nShowErrorInfoLabel,
  error,
  errorInfo,
}) => {
  const [showErrorInfo, setShowErrorInfo] = useState(false);
  const toggleErrorInfo = () => setShowErrorInfo(!showErrorInfo);
  return (
    <Container>
      <EmptyState>
        <EmptyState.Icon name={'error-circle-o'} />
        <EmptyState.Title>{i18nTitle}</EmptyState.Title>
        <EmptyState.Info>{i18nInfo}</EmptyState.Info>
        <EmptyState.Help>{i18nHelp}</EmptyState.Help>
        <EmptyState.Action>
          <ButtonLink
            data-testid={'unrecoverable-error-refresh-button'}
            href={'.'}
            as={'primary'}
            size={'lg'}
          >
            {i18nRefreshLabel}
          </ButtonLink>
        </EmptyState.Action>
        <EmptyState.Action secondary={true}>
          {error && (
            <>
              <ButtonLink
                data-testid={'unrecoverable-error-show-error-button'}
                onClick={toggleErrorInfo}
                style={{ marginBottom: 0 }}
              >
                {i18nShowErrorInfoLabel}
              </ButtonLink>
              &nbsp;
            </>
          )}
          <a
            data-testid={'unrecoverable-error-report-issue-link'}
            className={'btn btn-default'}
            href={
              'https://github.com/syndesisio/syndesis/issues/new?template=simple.md&labels=cat/bug&title=Error%20report'
            }
          >
            {i18nReportIssue}
          </a>
        </EmptyState.Action>
        {showErrorInfo && error && (
          <EmptyState.Help
            style={{
              background: 'rgba(255, 255, 255, 0.8)',
              border: '1px solid #dedede',
              marginTop: 10,
              padding: 10,
              textAlign: 'left',
            }}
          >
            {error.name}: {error.message}
            {errorInfo && (
              <pre style={{ marginTop: 10 }}>{errorInfo.componentStack}</pre>
            )}
          </EmptyState.Help>
        )}
      </EmptyState>
    </Container>
  );
};
