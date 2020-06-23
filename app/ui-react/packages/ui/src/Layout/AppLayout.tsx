import { LoadingPage } from '@rh-uxd/integration-react';
import React, { Suspense } from 'react';
import './AppLayout.css';

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
  username
}) => {

  const AppPage = React.lazy(() => import(('./AppPage')));
  const [isLoading, setIsLoading] = React.useState(true);

  const delayState = () => {
    setTimeout(() => {
      setIsLoading(false);
    }, 3000);
  };
  delayState();

  return (
    <div style={{position: 'relative'}}>
        <Suspense fallback={ isLoading && <LoadingPage appName="Syndesis"/>}>
          <AppPage
            avatar={avatar}
            pictograph={pictograph}
            verticalNav={verticalNav}
            logoOnClick={logoOnClick}
            showNavigation={showNavigation}
            logoutItem={logoutItem}
            onNavigationCollapse={onNavigationCollapse}
            onNavigationExpand={onNavigationExpand}
            onShowAboutModal={onShowAboutModal}
            onSelectSupport={onSelectSupport}
            onSelectConnectorsGuide={onSelectConnectorsGuide}
            onSelectContactUs={onSelectContactUs}
            onSelectSampleIntegrationTutorials={onSelectSampleIntegrationTutorials}
            onSelectUserGuide={onSelectUserGuide}
            username={username}
          />
        </Suspense>
    </div>
  );
};
