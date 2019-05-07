import * as React from 'react';
import { UIContext } from '../app';

/**
 * This component will automatically close the app left navigation bar when
 * mounted in the DOM, and will reopen it when unmounted.
 *
 * If you need this behaviour for a whole section, keep this component high in
 * the DOM tree, ideally before any component that will re-render on his
 * lifecycle - like data fetching components - to avoid firing quick unmount/mount
 * events that will lead to a bad UX.
 */
export const WithClosedNavigation: React.FunctionComponent = ({ children }) => {
  const context = React.useContext(UIContext);
  // we want the effect to be run only once, so we pass the empty array to the
  // useEffect and disable the eslint check
  React.useEffect(() => {
    context.hideNavigation();

    return () => {
      context.showNavigation();
    };
  }, []); // eslint-disable-line
  return <>{children}</>;
};
