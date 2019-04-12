import {
  Nav,
  NavList,
  Page,
  PageHeader,
  PageSection,
  PageSidebar,
} from '@patternfly/react-core';
import * as React from 'react';

export interface ILayoutBase {
  appTitle: string;
  pictograph: any;
  appNav: any;
  verticalNav: any[];
  logoHref: string;
  showNavigation: boolean;
  onNavigationCollapse(): void;
  onNavigationExpand(): void;
}

export interface ILayoutState {
  isNavOpen: boolean;
}

export class AppLayout extends React.Component<ILayoutBase, ILayoutState> {
  public onNavToggle: () => void;
  constructor(props: ILayoutBase) {
    super(props);
    this.state = {
      isNavOpen: true,
    };
    this.onNavToggle = () => {
      this.setState({
        isNavOpen: !this.state.isNavOpen,
      });
    };
  }

  public componentDidMount() {
    this.setState({
      isNavOpen: this.props.showNavigation,
    });
  }

  public render() {
    const { isNavOpen } = this.state;

    const Header = (
      <PageHeader
        logo={this.props.pictograph}
        logoProps={{ href: '/' }}
        toolbar={this.props.appNav}
        showNavToggle={true}
        isNavOpen={isNavOpen}
        onNavToggle={this.onNavToggle}
      />
    );
    const Sidebar = (
      <PageSidebar
        nav={
          <Nav>
            <NavList>{this.props.verticalNav}</NavList>
          </Nav>
        }
        isNavOpen={isNavOpen}
      />
    );

    return (
      <Page header={Header} sidebar={Sidebar}>
        <PageSection>{this.props.children}</PageSection>
      </Page>
    );
  }
}
