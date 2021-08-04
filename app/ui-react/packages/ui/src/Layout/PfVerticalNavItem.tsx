import { NavExpandable, NavItem } from '@patternfly/react-core';
import classNames from 'classnames';
import * as React from 'react';
import { Route } from 'react-router';
import { NavLink } from 'react-router-dom';

export interface IPfVerticalNavItem {
  className?: string;
  exact?: boolean;
  hidden?: boolean;
  isActive?: (match: any, location: any) => boolean;
  location?: any;
  strict?: boolean;
  to: string | any;
  label: any;
  children?: any;
}

function PfVerticalNavItem({
  className,
  exact,
  hidden,
  isActive: isActiveProp,
  location,
  strict,
  to,
  label,
  children,
  ...rest
}: IPfVerticalNavItem) {
  const path = typeof to === 'object' ? to.pathname : to;

  // Regex taken from: https://github.com/pillarjs/path-to-regexp/blob/master/index.js#L202
  const escapedPath = path && path.replace(/([.+*?=^!:${}()[\]|/\\])/g, '\\$1');

  const NavLinkViewTemplate = ({
    location: childLocation,
    match,
  }: {
    location: any;
    match: any;
  }) => {
    const isActive = !!(isActiveProp
      ? isActiveProp(match, childLocation)
      : match);

    if (hidden) {
      return null;
    }

    return children ? (
      <NavExpandable title={label} isActive={isActive} isExpanded={isActive}>
        {children}
      </NavExpandable>
    ) : (
      <NavItem>
        <NavLink
          to={to}
          className={
            classNames(
              // 'pf-c-nav__link',
              { ['pf-m-current']: isActive, }
            )
          }
          aria-current={isActive ? 'page' : undefined}
          {...rest}>
          {label}
        </NavLink>
      </NavItem>
    );
  };

  return (
    <Route
      path={escapedPath}
      exact={exact}
      strict={strict}
      location={location}
      children={NavLinkViewTemplate}
    />
  );
}

PfVerticalNavItem.displayName = 'VerticalNav.Item';

export { PfVerticalNavItem };
