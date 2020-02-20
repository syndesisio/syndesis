import { EmptyState, EmptyStateBody, EmptyStateVariant, Title } from '@patternfly/react-core';
import * as H from '@syndesis/history';
import * as React from 'react';
import { ButtonLink } from '../../Layout';

export interface ITagIntegrationDialogEmptyStateProps {
  href: H.LocationDescriptor;
  i18nTitle: string;
  i18nInfo: string;
  i18nGoToManageCiCdButtonText: string;
}

export class TagIntegrationDialogEmptyState extends React.Component<
  ITagIntegrationDialogEmptyStateProps
> {
  public render() {
    return (
      <EmptyState variant={EmptyStateVariant.full}>
        <Title headingLevel="h5" size="lg">
          {this.props.i18nTitle}
        </Title>
        <EmptyStateBody>{this.props.i18nInfo}</EmptyStateBody>
        <ButtonLink
          data-testid={'tag-integration-dialog-empty-state-manage-cicd-button'}
          as="primary"
          size="lg"
          href={this.props.href}
        >
          {this.props.i18nGoToManageCiCdButtonText}
        </ButtonLink>
      </EmptyState>
    );
  }
}
