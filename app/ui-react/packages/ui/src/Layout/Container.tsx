import classnames from 'classnames';
import * as React from 'react';

export interface IContainerProps {
  className?: string;
  [key: string]: any;
}

export const Container: React.FunctionComponent<IContainerProps> = ({
  children,
  className,
  ...props
}) => (
  <div className={classnames('container-fluid', className)} {...props}>
    {children}
  </div>
);
