import {
  Button,
  ButtonVariant,
  EmptyState,
  EmptyStateBody,
  EmptyStateIcon,
  EmptyStateVariant,
  Title,
} from '@patternfly/react-core';
import { AddCircleOIcon } from '@patternfly/react-icons';
import * as React from 'react';

export interface ICiCdListEmptyState {
  onAddNew: () => void;
  i18nTitle: string;
  i18nInfo: string;
  i18nAddNewButtonText: string;
}

export class CiCdListEmptyState extends React.Component<ICiCdListEmptyState> {
  public render() {
    return (
      <EmptyState variant={EmptyStateVariant.full}>
        <EmptyStateIcon icon={AddCircleOIcon} />
        <Title headingLevel="h5" size="lg">
          {this.props.i18nTitle}
        </Title>
        <EmptyStateBody>{this.props.i18nInfo}</EmptyStateBody>
        <Button
          data-testid={'cicd-list-empty-state-add-new-button'}
          isDisabled={false}
          variant={ButtonVariant.primary}
          onClick={this.props.onAddNew}
        >
          {this.props.i18nAddNewButtonText}
        </Button>
      </EmptyState>
    );
  }
}
