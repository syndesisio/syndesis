import { Nav, NavList, NavVariants } from '@patternfly/react-core';
import * as React from 'react';

export interface ITabBarProps {
  [key: string]: any;
}

export const TabBar: React.FunctionComponent<ITabBarProps> = ({
  children,
  ...props
}) => (
  <Nav {...props}>
    <NavList variant={NavVariants.tertiary}>{children}</NavList>
  </Nav>
);
