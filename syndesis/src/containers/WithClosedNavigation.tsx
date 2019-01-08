import * as React from 'react';
import { AppContext } from '../app';

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
