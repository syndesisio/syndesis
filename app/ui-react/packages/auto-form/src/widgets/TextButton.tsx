import * as React from 'react';

import './TextButton.css';

export interface ILinkButtonProps {
  linkText?: string;
  onClick: (event: React.MouseEvent<HTMLButtonElement>) => void;
  children?: React.ReactNode;
}

export class TextButton extends React.Component<ILinkButtonProps> {
  public render() {
    return (
      <>
        {this.props.linkText ? (
          <button
            type="button"
            onClick={this.props.onClick}
            className="btn btn-link"
            dangerouslySetInnerHTML={{ __html: this.props.linkText! }}
          />
        ) : (
          <button
            type="button"
            onClick={this.props.onClick}
            className="btn btn-link"
          >
            {this.props.children && this.props.children}
          </button>
        )}
      </>
    );
  }
}
