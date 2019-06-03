import {
  Avatar,
  Nav,
  NavList,
  Page,
  PageHeader,
  PageSidebar,
  Toolbar,
  ToolbarGroup,
  ToolbarItem,
} from '@patternfly/react-core';
import { css } from '@patternfly/react-styles';
import accessibleStyles from '@patternfly/patternfly/utilities/Accessibility/accessibility.css';
import * as React from 'react';
import { HelpDropdown } from '../Shared/HelpDropdown';
import { AppLayoutContext } from './AppLayoutContext';

export interface ILayoutBase {
  avatar: any;
  pictograph: any;
  appNav: any;
  verticalNav: any[];
  logoHref: string;
  logoutItem: React.ReactNode;
  showNavigation: boolean;
  onNavigationCollapse(): void;
  onNavigationExpand(): void;
  onShowAboutModal(): void;
  onSelectSupport(): void;
  onSelectSampleIntegrationTutorials(): void;
  onSelectUserGuide(): void;
  onSelectConnectorsGuide(): void;
  onSelectContactUs(): void;
}

export const AppLayout: React.FunctionComponent<ILayoutBase> = ({
  avatar,
  pictograph,
  appNav,
  verticalNav,
  logoHref,
  showNavigation,
  logoutItem,
  onNavigationCollapse,
  onNavigationExpand,
  onShowAboutModal,
  onSelectSupport,
  onSelectConnectorsGuide,
  onSelectContactUs,
  onSelectSampleIntegrationTutorials,
  onSelectUserGuide,
  children,
}) => {
  const onNavToggle = showNavigation
    ? onNavigationCollapse
    : onNavigationExpand;

  const [breadcrumb, setHasBreadcrumb] = React.useState(null);
  const showBreadcrumb = (b: any) => setHasBreadcrumb(b);

  return (
    <AppLayoutContext.Provider
      value={{
        showBreadcrumb,
      }}
    >
      <Page
        header={
          <PageHeader
            logo={pictograph}
            logoProps={{ href: logoHref }}
            toolbar={
              <Toolbar>
                <ToolbarGroup
                  className={css(
                    accessibleStyles.screenReader,
                    accessibleStyles.visibleOnLg
                  )}
                >
                  <ToolbarItem>
                    <HelpDropdown
                      className="syn-help-dropdown"
                      isOpen={false}
                      launchSupportPage={onSelectSupport}
                      launchAboutModal={onShowAboutModal}
                      launchSampleIntegrationTutorials={
                        onSelectSampleIntegrationTutorials
                      }
                      launchConnectorsGuide={onSelectConnectorsGuide}
                      launchUserGuide={onSelectUserGuide}
                      launchContactUs={onSelectContactUs}
                      additionalDropdownItems={[logoutItem]}
                    />
                  </ToolbarItem>
                </ToolbarGroup>
                <ToolbarGroup>
                  <ToolbarItem className="pf-u-display-none pf-u-display-block-on-lg">
                    {appNav}
                  </ToolbarItem>
                </ToolbarGroup>
              </Toolbar>
            }
            avatar={<Avatar src={avatar} alt="User Avatar" />}
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
        breadcrumb={breadcrumb}
      >
        {children}
      </Page>
    </AppLayoutContext.Provider>
  );
};
