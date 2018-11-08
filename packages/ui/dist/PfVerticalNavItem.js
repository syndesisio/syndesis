import classNames from 'classnames';
import { VerticalNav } from 'patternfly-react';
import * as React from 'react';
import { Route } from 'react-router';
import { Link } from 'react-router-dom';
function PfVerticalNavItem({ className, exact, isActive: isActiveProp, icon, location, strict, to, label, children, ...rest }) {
    const path = typeof to === 'object' ? to.pathname : to;
    // Regex taken from: https://github.com/pillarjs/path-to-regexp/blob/master/index.js#L202
    const escapedPath = path && path.replace(/([.+*?=^!:${}()[\]|/\\])/g, '\\$1');
    const NavLinkChildren = ({ location: childLocation, match }) => {
        const isActive = !!(isActiveProp
            ? isActiveProp(match, childLocation)
            : match);
        return (React.createElement("li", { className: classNames('list-group-item', {
                active: isActive
            }) },
            React.createElement(Link, Object.assign({ to: to, children: React.createElement(React.Fragment, null,
                    React.createElement("span", { className: icon }),
                    React.createElement("span", { className: "list-group-item-value" }, label),
                    React.createElement("div", { className: "badge-container-pf" })) }, rest)),
            children));
    };
    return (React.createElement(Route, { path: escapedPath, exact: exact, strict: strict, location: location, children: NavLinkChildren }));
}
PfVerticalNavItem.displayName = VerticalNav.Item.displayName;
export { PfVerticalNavItem };
//# sourceMappingURL=PfVerticalNavItem.js.map