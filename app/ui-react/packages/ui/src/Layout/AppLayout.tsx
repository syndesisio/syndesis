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

export const AppLayout: React.FunctionComponent<ILayoutBase> = ({
  pictograph,
  appNav,
  verticalNav,
  logoHref,
  showNavigation,
  onNavigationCollapse,
  onNavigationExpand,
  children,
}) => {
  const onNavToggle = showNavigation
    ? onNavigationCollapse
    : onNavigationExpand;
  return (
    <Page
      header={
        <PageHeader
          logo={pictograph}
          logoProps={{ href: logoHref }}
          toolbar={appNav}
          showNavToggle={true}
          isNavOpen={showNavigation}
          onNavToggle={onNavToggle}
        />
      }
      sidebar={
        <PageSidebar
          nav={
            <Nav>
              <NavList>{verticalNav}</NavList>
            </Nav>
          }
          isNavOpen={showNavigation}
        />
      }
    >
      <PageSection>{children}</PageSection>
    </Page>
  );
};
