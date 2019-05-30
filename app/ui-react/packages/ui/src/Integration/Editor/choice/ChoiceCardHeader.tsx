import * as React from 'react';

export interface IChoiceCardHeaderProps {
  i18nConditions: string;
  i18nManage: string;
  i18nApply: string;
  isValid: boolean;
  mode: 'view' | 'edit';
  onClickManage: () => void;
  onClickApply: () => void;
}

export class ChoiceCardHeader extends React.Component<IChoiceCardHeaderProps> {
  public render() {
    return (
      <h2 className="card-pf-title">
        {this.props.i18nConditions}
        {this.props.mode === 'view' && (
          <button
            data-testid="choice-card-header-manage-button"
            className="btn btn-default pull-right"
            onClick={this.props.onClickManage}
          >
            {this.props.i18nManage}
          </button>
        )}
        {this.props.mode === 'edit' && (
          <button
            data-testid="choice-card-header-apply-button"
            className="btn btn-default pull-right"
            onClick={this.props.onClickApply}
            disabled={!this.props.isValid}
          >
            {this.props.i18nApply}
          </button>
        )}
      </h2>
    );
  }
}
