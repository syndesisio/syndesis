import classnames from 'classnames';
import * as H from 'history';
import * as React from 'react';
import { Link } from 'react-router-dom';

interface IButtonLinkProps {
  onClick?: (e: React.MouseEvent<any>) => void;
  href?: H.LocationDescriptor;
  className?: string;
  disabled?: boolean;
  as?:
    | 'default'
    | 'primary'
    | 'success'
    | 'info'
    | 'warning'
    | 'danger'
    | 'link';
}

export const ButtonLink: React.FunctionComponent<IButtonLinkProps> = ({
  onClick,
  href,
  className,
  disabled,
  as = 'default',
  children,
}) => {
  className = classnames('btn', `btn-${as}`, className);
  return href ? (
    <Link to={href} onClick={onClick} className={className}>
      {children}
    </Link>
  ) : (
    <button
      onClick={onClick}
      className={className}
      disabled={disabled || (!onClick && !href)}
    >
      {children}
    </button>
  );
};
