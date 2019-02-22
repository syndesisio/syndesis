import * as H from 'history';
import * as React from 'react';
import { PfNavLink } from './PfNavLink';

export interface ITabBarItemProps {
  label: string;
  to: H.LocationDescriptor;
  [key: string]: any;
}

export const TabBarItem: React.FunctionComponent<ITabBarItemProps> = ({
  label,
  to,
  ...props
}) => <PfNavLink label={label} to={to} {...props} />;
