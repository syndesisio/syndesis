import { Nav } from 'patternfly-react';
import * as React from 'react';

export interface ITabBarProps {
  [key: string]: any;
}

export const TabBar: React.FunctionComponent<ITabBarProps> = ({
  children,
  ...props
}) => (
  <Nav bsClass="nav nav-tabs nav-tabs-pf" {...props}>
    {children}
  </Nav>
);
