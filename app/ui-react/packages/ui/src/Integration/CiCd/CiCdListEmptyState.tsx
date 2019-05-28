import { Button, EmptyState } from 'patternfly-react';
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
      <EmptyState>
        <EmptyState.Icon />
        <EmptyState.Title>{this.props.i18nTitle}</EmptyState.Title>
        <EmptyState.Info>{this.props.i18nInfo}</EmptyState.Info>
        <EmptyState.Action>
          <Button
            data-testid={'cicd-list-empty-state-add-new-button'}
            bsStyle="primary"
            bsSize="large"
            onClick={this.props.onAddNew}
          >
            {this.props.i18nAddNewButtonText}
          </Button>
        </EmptyState.Action>
      </EmptyState>
    );
  }
}
