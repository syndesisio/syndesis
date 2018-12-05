import { Masthead, VerticalNav } from 'patternfly-react';
import * as React from 'react';
import { PfVerticalNavItem } from './PfVerticalNavItem';

export interface ILayoutBase {
  appTitle: string;
  pictograph: string;
  typogram: string;
  appNav: any;
  verticalNav: PfVerticalNavItem[];
  logoHref: string;
}

export class AppLayout extends React.Component<ILayoutBase> {
  public render() {
    return (
      <React.Fragment>
        <VerticalNav sessionKey={'mainNav'}>
          <VerticalNav.Masthead
            iconImg={this.props.pictograph}
            titleImg={this.props.typogram}
            title={this.props.appTitle}
            href={this.props.logoHref}
          >
            <Masthead.Collapse>{this.props.appNav}</Masthead.Collapse>
          </VerticalNav.Masthead>
          {this.props.verticalNav}
        </VerticalNav>
        <div className="container-pf-nav-pf-vertical">
          {this.props.children}
        </div>
      </React.Fragment>
    );
  }
}
