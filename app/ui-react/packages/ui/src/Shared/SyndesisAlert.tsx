import { Alert, ExpandableSection } from '@patternfly/react-core';
import * as React from 'react';

export enum SyndesisAlertLevel {
  ERROR = 'error',
  WARN = 'warning',
  INFO = 'info',
}

export interface ISyndesisAlertProps {
  level: SyndesisAlertLevel;
  message: string;
  detail?: string;
  i18nTextExpanded?: string;
  i18nTextCollapsed?: string;
}

function mapLevel(incoming: SyndesisAlertLevel) {
  switch (incoming) {
    case SyndesisAlertLevel.ERROR:
      return 'danger';
    default:
      return incoming;
  }
}

export const SyndesisAlert: React.FunctionComponent<ISyndesisAlertProps> = ({
  level,
  message,
  detail,
  i18nTextExpanded,
  i18nTextCollapsed,
}) => {
  return (
    <Alert
      isInline={true}
      variant={mapLevel(level)}
      title={
        <span
          dangerouslySetInnerHTML={{
            __html: message,
          }}
        />
      }
    >
      {detail && (
        <ExpandableSection
          toggleTextExpanded={i18nTextExpanded}
          toggleTextCollapsed={i18nTextCollapsed}
        >
          <pre>{detail}</pre>
        </ExpandableSection>
      )}
    </Alert>
  );
};
