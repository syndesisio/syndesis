import accessibleStyles from '@patternfly/patternfly/utilities/Accessibility/accessibility.css';
import {
  Avatar,
  DropdownItem,
  DropdownSeparator,
  Nav,
  NavList,
  Page,
  PageHeader,
  PageSidebar,
  SkipToContent,
  Toolbar,
  ToolbarGroup,
  ToolbarItem,
} from '@patternfly/react-core';
import { css } from '@patternfly/react-styles';
import { global_breakpoint_lg } from '@patternfly/react-tokens';
import * as React from 'react';
import { HelpDropdown } from '../Shared/HelpDropdown';
import { AppLayoutContext } from './AppLayoutContext';
import { AppTopMenu } from './AppTopMenu';

export interface ILayoutBase {
  avatar?: string;
  pictograph: any;
  verticalNav: any[];
  logoOnClick: () => void;
  logoutItem: {
    key: string;
    onClick: () => Promise<any>;
    id: string;
    className?: string;
    children: string;
  };
  showNavigation: boolean;
  username: string;
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
  verticalNav,
  logoOnClick,
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
  username,
  children,
}) => {
  const onNavToggle = showNavigation
    ? onNavigationCollapse
    : onNavigationExpand;

  const [breadcrumb, setHasBreadcrumb] = React.useState(null);
  const showBreadcrumb = (b: any) => setHasBreadcrumb(b);
  const PageSkipToContent = (
    <SkipToContent href="#main-content-page-layout-default-nav">
      Skip to Content
    </SkipToContent>
  );
  const [isMobileView, setIsMobileView] = React.useState(false);
  const [isNavOpenMobile, setIsNavOpenMobile] = React.useState(false);
  const onNavToggleMobile = () => {
    setIsNavOpenMobile(!isNavOpenMobile);
  };
  const [curViewportWidth, setCurViewportWidth] = React.useState(1024);
  const onPageResize = (props: { mobileView: boolean; windowSize: number }) => {
    setIsMobileView(props.mobileView);
    setCurViewportWidth(props.windowSize);
  };
  const [isTabletView, setIsTabletView] = React.useState(false);
  const LARGE_VIEWPORT_BREAKPOINT = parseInt(
    global_breakpoint_lg.value.substring(
      0,
      global_breakpoint_lg.value.indexOf('px')
    ),
    10
  );
  React.useEffect(() => {
    setIsTabletView(curViewportWidth <= LARGE_VIEWPORT_BREAKPOINT);
  }, [curViewportWidth]);
  return (
    <AppLayoutContext.Provider
      value={{
        showBreadcrumb,
      }}
    >
      <Page
        skipToContent={PageSkipToContent}
        onPageResize={onPageResize}
        header={
          <PageHeader
            logo={pictograph}
            logoProps={{ onClick: logoOnClick }}
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
                      isTabletView={isTabletView}
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
                      additionalDropdownItems={[
                        <DropdownSeparator
                          key="separator"
                          className="pf-u-display-none-on-lg"
                        />,
                        <DropdownItem
                          key={`mobile-${logoutItem.key}`}
                          onClick={logoutItem.onClick}
                        >
                          <button
                            type="button"
                            role="menuitem"
                            id={`mobile-${logoutItem.id}`}
                            data-testid={`mobile-${logoutItem.id}`}
                            className={`${
                              logoutItem.className
                            } pf-u-display-none-on-lg`}
                          >
                            {logoutItem.children}
                          </button>
                        </DropdownItem>,
                      ]}
                    />
                  </ToolbarItem>
                  <ToolbarItem className="pf-u-display-none pf-u-display-block-on-lg">
                    <AppTopMenu username={username}>
                      <DropdownItem
                        key={logoutItem.key}
                        onClick={logoutItem.onClick}
                      >
                        <button
                          type="button"
                          role="menuitem"
                          id={logoutItem.id}
                          data-testid={logoutItem.id}
                          className={logoutItem.className}
                        >
                          {logoutItem.children}
                        </button>
                      </DropdownItem>
                    </AppTopMenu>
                  </ToolbarItem>
                </ToolbarGroup>
              </Toolbar>
            }
            avatar={avatar && <Avatar src={avatar} alt="User Avatar" />}
            showNavToggle={true}
            onNavToggle={isMobileView ? onNavToggleMobile : onNavToggle}
            isNavOpen={isMobileView ? isNavOpenMobile : showNavigation}
          />
        }
        sidebar={
          <PageSidebar
            nav={
              <Nav>
                <NavList>{verticalNav}</NavList>
              </Nav>
            }
            isNavOpen={isMobileView ? isNavOpenMobile : showNavigation}
          />
        }
        breadcrumb={breadcrumb}
      >
        {children}
      </Page>
    </AppLayoutContext.Provider>
  );
};
