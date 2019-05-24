// tslint:disable react-unused-props-and-state
// remove the above line after this goes GA https://github.com/Microsoft/tslint-microsoft-contrib/pull/824
import * as H from '@syndesis/history';
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
