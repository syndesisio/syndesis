import * as React from 'react';
import { AppContext } from '../app';

/**
 * This component will automatically close the app left navigation bar when
 * mounted in the DOM, and will reopen it when unmounted.
 *
 * If you need this behaviour for a whole section, keep this component high in
 * the DOM tree, ideally before any component that will re-render on his
 * lifecycle - like data fetching components - to avoid firing quick unmount/mount
 * events that will lead to a bad UX.
 */
export class WithClosedNavigation extends React.Component {
  static contextType = AppContext;

  public componentDidMount(): void {
    this.context.hideNavigation();
  }

  public componentWillUnmount(): void {
    this.context.showNavigation();
  }

  public render() {
    return this.props.children;
  }
}
