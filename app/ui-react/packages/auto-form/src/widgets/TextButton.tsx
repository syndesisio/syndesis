import * as React from 'react';

import './TextButton.css';

export interface ITextButtonProps {
  linkText?: string;
  onClick: (event: React.MouseEvent<HTMLButtonElement>) => void;
  children?: React.ReactNode;
  enable?: boolean;
}

export class TextButton extends React.Component<ITextButtonProps> {
  public render() {
    const enable =
      typeof this.props.enable !== 'undefined' ? this.props.enable : true;
    return (
      <>
        {this.props.linkText ? (
          <button
            type="button"
            onClick={this.props.onClick}
            className="btn btn-link"
            dangerouslySetInnerHTML={{ __html: this.props.linkText! }}
            disabled={!enable}
          />
        ) : (
          <button
            type="button"
            onClick={this.props.onClick}
            className="btn btn-link"
            disabled={!enable}
          >
            {this.props.children && this.props.children}
          </button>
        )}
      </>
    );
  }
}
