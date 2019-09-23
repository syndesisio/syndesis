import * as React from 'react';

import './TextButton.css';

export interface ITextButtonProps extends React.HTMLAttributes<HTMLElement> {
  linkText?: string;
  enable?: boolean;
}

export const TextButton: React.FunctionComponent<ITextButtonProps> = ({ className = '', enable = true, linkText, ...props }) => {
  const innerHtml = linkText ? { __html: linkText } : undefined;
  return (
    <button
      type="button"
      className={`btn btn-link ${className}`}
      dangerouslySetInnerHTML={innerHtml}
      disabled={!enable}
      {...props}
    />
  );
}
