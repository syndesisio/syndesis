import {
  EmptyState,
  EmptyStateBody,
  EmptyStateIcon,
  EmptyStateVariant,
  Title,
  Tooltip,
} from '@patternfly/react-core';
import { AddCircleOIcon } from '@patternfly/react-icons';
import * as H from '@syndesis/history';
import * as React from 'react';
import { ButtonLink } from '../Layout';

export interface IIntegrationsEmptyStateProps {
  i18nCreateIntegration: string;
  i18nCreateIntegrationTip?: string;
  i18nEmptyStateInfo: string;
  i18nEmptyStateTitle: string;
  linkCreateIntegration: H.LocationDescriptor;
}

export const IntegrationsEmptyState: React.FunctionComponent<IIntegrationsEmptyStateProps> = ({
  i18nCreateIntegration,
  i18nCreateIntegrationTip,
  i18nEmptyStateInfo,
  i18nEmptyStateTitle,
  linkCreateIntegration,
}) => (
  <EmptyState variant={EmptyStateVariant.full}>
    <EmptyStateIcon icon={AddCircleOIcon} />
    <Title headingLevel="h5" size="lg">
      {i18nEmptyStateTitle}
    </Title>
    <EmptyStateBody>{i18nEmptyStateInfo}</EmptyStateBody>
    <Tooltip
      content={
        i18nCreateIntegrationTip
          ? i18nCreateIntegrationTip
          : i18nCreateIntegration
      }
    >
      <>
        <br />
        <ButtonLink
          data-testid={'integrations-empty-state-create-button'}
          href={linkCreateIntegration}
          as={'primary'}
        >
          {i18nCreateIntegration}
        </ButtonLink>
      </>
    </Tooltip>
  </EmptyState>
);
