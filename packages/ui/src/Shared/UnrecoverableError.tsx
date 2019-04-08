import { EmptyState } from 'patternfly-react';
import { useState } from 'react';
import * as React from 'react';
import { ButtonLink, Container } from '../Layout';

export interface IUnrecoverableErrorProps {
  i18nTitle: string;
  i18nInfo: string;
  i18nHelp: string;
  i18nRefreshLabel: string;
  i18nShowErrorInfoLabel: string;
  error?: Error;
  errorInfo?: React.ErrorInfo;
}

export const UnrecoverableError: React.FC<IUnrecoverableErrorProps> = ({
  i18nTitle,
  i18nInfo,
  i18nHelp,
  i18nRefreshLabel,
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
          <ButtonLink href={'.'} as={'primary'} size={'lg'}>
            {i18nRefreshLabel}
          </ButtonLink>
        </EmptyState.Action>
        {error && (
          <EmptyState.Action secondary={true}>
            <ButtonLink onClick={toggleErrorInfo}>
              {i18nShowErrorInfoLabel}
            </ButtonLink>
          </EmptyState.Action>
        )}
        {showErrorInfo && error && (
          <EmptyState.Help style={{ textAlign: 'left' }}>
            <p>
              {error.name}: {error.message}
            </p>
            {errorInfo && <pre>{errorInfo.componentStack}</pre>}
          </EmptyState.Help>
        )}
      </EmptyState>
    </Container>
  );
};
