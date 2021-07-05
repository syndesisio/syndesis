import {
  Button,
  Card,
  CardBody,
  EmptyState,
  EmptyStateBody,
  EmptyStateIcon,
  EmptyStatePrimary,
  EmptyStateSecondaryActions,
  EmptyStateVariant,
  ExpandableSection,
  Text,
  Title,
} from '@patternfly/react-core';
import { ErrorCircleOIcon, WarningTriangleIcon } from '@patternfly/react-icons';
import { useState } from 'react';
import * as React from 'react';

export enum UnrecoverableErrorIcon {
  ERROR,
  WARNING,
}

export interface IUnrecoverableErrorProps {
  actions?: React.ReactNode;
  i18nTitle: string;
  i18nInfo: string;
  i18nHelp?: string;
  i18nRefreshLabel?: string;
  i18nReportIssue?: string;
  i18nShowErrorInfoLabel?: string;
  icon?: UnrecoverableErrorIcon;
  error?: Error;
  errorInfo?: React.ErrorInfo;
  secondaryActions?: React.ReactNodeArray | false;
}

/**
 * Helper function to customize the error page icon
 * @param icon
 */
function useIcon(icon?: UnrecoverableErrorIcon) {
  switch (icon) {
    case UnrecoverableErrorIcon.WARNING:
      return WarningTriangleIcon;
    default:
      return ErrorCircleOIcon;
  }
}

function useIconColor(icon?: UnrecoverableErrorIcon) {
  switch (icon) {
    case UnrecoverableErrorIcon.WARNING:
      return '#f0ab00';
    default:
      return '#c9190b';
  }
}

export const UnrecoverableError: React.FC<IUnrecoverableErrorProps> = ({
  actions,
  i18nTitle,
  i18nInfo,
  i18nHelp,
  i18nRefreshLabel,
  i18nReportIssue,
  i18nShowErrorInfoLabel,
  icon,
  error,
  errorInfo,
  secondaryActions,
}) => {
  const [showErrorInfo, setShowErrorInfo] = useState(false);
  const toggleErrorInfo = () => setShowErrorInfo(!showErrorInfo);
  return (
    <Card>
      <CardBody>
        <EmptyState variant={EmptyStateVariant.full}>
          <EmptyStateIcon color={useIconColor(icon)} icon={useIcon(icon)} />
          <Title headingLevel="h5" size="lg">
            {i18nTitle}
          </Title>
          <EmptyStateBody>
            <Text>{i18nInfo}</Text>
            <Text>{i18nHelp}</Text>
            {error && (
              <ExpandableSection
                isExpanded={showErrorInfo}
                onToggle={toggleErrorInfo}
                toggleText={i18nShowErrorInfoLabel}
              >
                <Text component={'p'} className={'pf-u-text-align-left'}>
                  {error.name}: {error.message}
                  {errorInfo && (
                    <Text component={'pre'}>{errorInfo.componentStack}</Text>
                  )}
                </Text>
              </ExpandableSection>
            )}
          </EmptyStateBody>
          <EmptyStatePrimary>
            {typeof actions !== 'undefined' ? (
              actions!
            ) : (
              <>
                <Button
                  data-testid={'unrecoverable-error-refresh-button'}
                  variant={'primary'}
                  onClick={() => window.location.reload()}
                >
                  {i18nRefreshLabel!}
                </Button>
              </>
            )}
          </EmptyStatePrimary>
          <EmptyStateSecondaryActions>
            {typeof secondaryActions !== 'undefined' ? (
              <>{secondaryActions && secondaryActions}</>
            ) : (
              <>
                <Button
                  component={'a'}
                  variant={'secondary'}
                  data-testid={'unrecoverable-error-report-issue-link'}
                  href={
                    'https://github.com/syndesisio/syndesis/issues/new?template=simple.md&labels=cat/bug&title=Error%20report'
                  }
                >
                  {i18nReportIssue}
                </Button>
              </>
            )}
          </EmptyStateSecondaryActions>
        </EmptyState>
      </CardBody>
    </Card>
  );
};
