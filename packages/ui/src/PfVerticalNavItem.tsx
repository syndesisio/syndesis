import classNames from 'classnames';
import { VerticalNav } from 'patternfly-react';
import * as React from 'react';
import { Route } from 'react-router';
import { Link } from 'react-router-dom';

interface IPfVerticalNavItem {
  className?: string;
  exact?: boolean;
  isActive?: (match: any, location: any) => boolean;
  icon: string;
  location?: any;
  strict?: boolean;
  to: string | any;
  label: any;
  children?: any;
}

function PfVerticalNavItem({
  className,
  exact,
  isActive: isActiveProp,
  icon,
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

  const NavLinkChildren = ({
    location: childLocation,
    match
  }: {
    location: any;
    match: any;
  }) => {
    const isActive = !!(isActiveProp
      ? isActiveProp(match, childLocation)
      : match);

    return (
      <li
        className={classNames('list-group-item', {
          active: isActive
        })}
      >
        <Link
          to={to}
          children={
            <React.Fragment>
              <span className={icon} />
              <span className="list-group-item-value">{label}</span>
              <div className="badge-container-pf" />
            </React.Fragment>
          }
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
}

PfVerticalNavItem.displayName = VerticalNav.Item.displayName;

export { PfVerticalNavItem };
