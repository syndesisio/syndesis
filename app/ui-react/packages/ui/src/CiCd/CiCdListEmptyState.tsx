import { Button, EmptyState } from 'patternfly-react';
import * as React from 'react';

export interface ICiCdListEmptyState {
  i18nTitle: string;
  i18nAddNewButtonText: string;
}

export class CiCdListEmptyState extends React.Component<ICiCdListEmptyState> {
  public render() {
    return (
      <EmptyState>
        <EmptyState.Icon />
        <EmptyState.Title>{this.props.i18nTitle}</EmptyState.Title>
        <EmptyState.Info />
        <EmptyState.Action>
          <Button bsStyle="primary" bsSize="large">
            {this.props.i18nAddNewButtonText}
          </Button>
        </EmptyState.Action>
      </EmptyState>
    );
  }
}
