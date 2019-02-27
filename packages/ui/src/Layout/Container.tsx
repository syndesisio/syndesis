import * as React from 'react';

export interface IContainerProps {
  [key: string]: any;
}

export const Container: React.FunctionComponent<IContainerProps> = ({
  children,
  ...props
}) => (
  <div className="container-fluid" {...props}>
    {children}
  </div>
);
