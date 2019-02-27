// tslint:disable react-unused-props-and-state
// remove the above line after this goes GA https://github.com/Microsoft/tslint-microsoft-contrib/pull/824
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
