import * as H from '@syndesis/history';
import * as React from 'react';
import { Route } from 'react-router';
import { Link } from 'react-router-dom';

interface INavLink {
  activeClassName?: string;
  activeStyle?: any;
  className?: string;
  exact?: boolean;
  isActive?: (match: any, location: any) => boolean;
  location?: any;
  strict?: boolean;
  style?: any;
  to: H.LocationDescriptor;
  label: any;
  children?: any;
  [key: string]: any;
}

function joinClassnames(...classnames: Array<string | undefined>): string {
  return classnames.filter(i => i).join(' ');
}

export const PfNavLink = ({
  activeClassName = 'pf-m-current',
  activeStyle,
  className: classNameProp = 'pf-c-nav__link',
  exact,
  isActive: isActiveProp,
  location,
  strict,
  style: styleProp,
  to,
  label,
  children,
  activeKey,
  activeHref,
  ...rest
}: INavLink) => {
  const path = typeof to === 'object' ? to.pathname : to;
  // Regex taken from: https://github.com/pillarjs/path-to-regexp/blob/master/index.js#L202
  const escapedPath = path && path.replace(/([.+*?=^!:${}()[\]|/\\])/g, '\\$1');
  const NavLinkChildren = ({
    location: childLocation,
    match,
  }: {
    location: any;
    match: any;
  }) => {
    const isActive = !!(isActiveProp
      ? isActiveProp(match, childLocation)
      : match);
    const classname = isActive
      ? joinClassnames(classNameProp, activeClassName)
      : classNameProp;
    const style = isActive ? activeStyle : styleProp;
    return (
      <li className={'pf-c-nav__item'}>
        <Link
          style={style}
          className={classname}
          to={to}
          children={label}
          {...rest}
        />
        {children}
      </li>
    );
  };
  return (
    <Route
      path={escapedPath}
      exact={exact}
      strict={strict}
      location={location}
      children={NavLinkChildren}
    />
  );
};
