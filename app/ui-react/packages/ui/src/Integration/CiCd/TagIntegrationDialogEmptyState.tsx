import * as H from '@syndesis/history';
import { EmptyState } from 'patternfly-react';
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
      <EmptyState
        style={
          {
            background: 'inherit',
            border: 'none',
          } /* todo component specific override */
        }
      >
        <EmptyState.Icon />
        <EmptyState.Title>{this.props.i18nTitle}</EmptyState.Title>
        <EmptyState.Info>{this.props.i18nInfo}</EmptyState.Info>
        <EmptyState.Action>
          <ButtonLink
            data-testid={
              'tag-integration-dialog-empty-state-manage-cicd-button'
            }
            as="primary"
            size="lg"
            href={this.props.href}
          >
            {this.props.i18nGoToManageCiCdButtonText}
          </ButtonLink>
        </EmptyState.Action>
      </EmptyState>
    );
  }
}
