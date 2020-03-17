// tslint:disable react-unused-props-and-state
// remove the above line after this goes GA https://github.com/Microsoft/tslint-microsoft-contrib/pull/824
import * as H from '@syndesis/history';
import classnames from 'classnames';
import * as React from 'react';
import { Link } from 'react-router-dom';

export interface IButtonLinkProps {
  onClick?: (
    e: MouseEvent | React.MouseEvent<any> | React.KeyboardEvent<Element>
  ) => void;
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
  size?: 'lg' | 'sm' | 'xs';
  [key: string]: any;
}

export const ButtonLink = React.forwardRef<any, IButtonLinkProps>(
  (
    {
      onClick,
      href,
      className,
      disabled,
      as = 'default',
      size,
      children,
      ...props
    },
    ref
  ) => {
    const onClickInterceptor = (ev: React.MouseEvent) => {
      if (onClick) {
        ev.preventDefault();
        ev.stopPropagation();
        onClick(ev);
      }
    };

    className = classnames(
      'pf-c-button',
      {
        'pf-m-danger': as === 'danger' || as === 'warning',
        'pf-m-link': as === 'link',
        'pf-m-primary': as === 'primary' || as === 'success',
        'pf-m-secondary': as === 'default' || as === 'info',
      },
      className,
      {
        'btn-lg': size === 'lg',
        'btn-sm': size === 'sm',
        'btn-xs': size === 'xs',
      }
    );
    return href && !disabled ? (
      <Link
        to={href}
        onClick={onClickInterceptor}
        className={className}
        ref={ref}
        {...props}
      >
        {children}
      </Link>
    ) : (
      <button
        onClick={onClickInterceptor}
        className={className}
        disabled={disabled || (!onClick && !href)}
        ref={ref}
        {...props}
      >
        {children}
      </button>
    );
  }
);
