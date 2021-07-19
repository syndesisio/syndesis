import { Nav, NavList } from '@patternfly/react-core';
import * as React from 'react';

export interface ITabBarProps {
  [key: string]: any;
}

export const TabBar: React.FunctionComponent<ITabBarProps> = ({
  children,
  ...props
}) => (
  <Nav variant={'tertiary'} {...props}>
    <NavList>{children}</NavList>
  </Nav>
);
