import * as H from '@syndesis/history';
import { EmptyState, OverlayTrigger, Tooltip } from 'patternfly-react';
import * as React from 'react';
import { ButtonLink } from '../Layout';

export interface IIntegrationsEmptyStateProps {
  i18nCreateIntegration: string;
  i18nCreateIntegrationTip?: string;
  i18nEmptyStateInfo: string;
  i18nEmptyStateTitle: string;
  linkCreateIntegration: H.LocationDescriptor;
}

export class IntegrationsEmptyState extends React.Component<
  IIntegrationsEmptyStateProps
> {
  public getCreateIntegrationTooltip() {
    return (
      <Tooltip id="createTip">
        {this.props.i18nCreateIntegrationTip
          ? this.props.i18nCreateIntegrationTip
          : this.props.i18nCreateIntegration}
      </Tooltip>
    );
  }

  public render() {
    return (
      <EmptyState>
        <EmptyState.Icon />
        <EmptyState.Title>{this.props.i18nEmptyStateTitle}</EmptyState.Title>
        <EmptyState.Info>{this.props.i18nEmptyStateInfo}</EmptyState.Info>
        <EmptyState.Action>
          <OverlayTrigger
            overlay={this.getCreateIntegrationTooltip()}
            placement="top"
          >
            <ButtonLink
              data-testid={'integrations-empty-state-create-button'}
              href={this.props.linkCreateIntegration}
              as={'primary'}
            >
              {this.props.i18nCreateIntegration}
            </ButtonLink>
          </OverlayTrigger>
        </EmptyState.Action>
      </EmptyState>
    );
  }
}
