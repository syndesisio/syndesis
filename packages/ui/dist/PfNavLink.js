import * as React from 'react';
import { Route } from 'react-router';
import { Link } from 'react-router-dom';
function joinClassnames(...classnames) {
    return classnames.filter(i => i).join(' ');
}
export const PfNavLink = ({ activeClassName = 'active', activeStyle, className: classNameProp, exact, isActive: isActiveProp, location, strict, style: styleProp, to, label, children, ...rest }) => {
    const path = typeof to === 'object' ? to.pathname : to;
    // Regex taken from: https://github.com/pillarjs/path-to-regexp/blob/master/index.js#L202
    const escapedPath = path && path.replace(/([.+*?=^!:${}()[\]|/\\])/g, '\\$1');
    const NavLinkChildren = ({ location: childLocation, match }) => {
        const isActive = !!(isActiveProp
            ? isActiveProp(match, childLocation)
            : match);
        const className = isActive
            ? joinClassnames(classNameProp, activeClassName)
            : classNameProp;
        const style = isActive ? { ...styleProp, ...activeStyle } : styleProp;
        return (React.createElement("li", { className: className },
            React.createElement(Link, Object.assign({ style: style, to: to, children: label }, rest)),
            children));
    };
    return (React.createElement(Route, { path: escapedPath, exact: exact, strict: strict, location: location, children: NavLinkChildren }));
};
//# sourceMappingURL=PfNavLink.js.map