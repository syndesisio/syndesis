import * as React from 'react';

import './TextButton.css';

export interface ITextButtonProps {
  linkText?: string;
  onClick: (event: React.MouseEvent<HTMLButtonElement>) => void;
  children?: React.ReactNode;
  visible?: boolean;
}

export class TextButton extends React.Component<ITextButtonProps> {
  public render() {
    const visible =
      typeof this.props.visible !== 'undefined' ? this.props.visible : true;
    const style: React.CSSProperties = visible
      ? { visibility: 'visible' }
      : { visibility: 'hidden' };
    return (
      <>
        {this.props.linkText ? (
          <button
            type="button"
            style={style}
            onClick={this.props.onClick}
            className="btn btn-link"
            dangerouslySetInnerHTML={{ __html: this.props.linkText! }}
          />
        ) : (
          <button
            type="button"
            style={style}
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
