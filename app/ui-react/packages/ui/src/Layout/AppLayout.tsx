import {
  Nav,
  NavList,
  Page,
  PageHeader,
  PageSidebar,
} from '@patternfly/react-core';
import * as React from 'react';
import { HelpDropdown } from '../Shared/HelpDropdown';
import { AppLayoutContext } from './AppLayoutContext';

export interface ILayoutBase {
  pictograph: any;
  appNav: any;
  verticalNav: any[];
  logoHref: string;
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
  pictograph,
  appNav,
  verticalNav,
  logoHref,
  showNavigation,
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
              <>
                {
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
                  />
                }
                {appNav}
              </>
            }
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
