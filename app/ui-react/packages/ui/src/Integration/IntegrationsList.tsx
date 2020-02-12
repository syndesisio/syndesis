import { DataList } from '@patternfly/react-core';
import * as React from 'react';

export interface IIntegrationsListProps {
  i18nAriaLabel: string;
}
export const IntegrationsList: React.FunctionComponent<IIntegrationsListProps> = ({
  i18nAriaLabel,
  children,
}) => <DataList aria-label={i18nAriaLabel}>{children}</DataList>;
