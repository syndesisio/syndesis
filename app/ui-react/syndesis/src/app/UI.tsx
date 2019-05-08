import { WithApiVersion } from '@syndesis/api';
import {
  AppLayout,
  AppTopMenu,
  INotification,
  INotificationType,
  Notifications,
  PfDropdownItem,
  PfVerticalNavItem,
} from '@syndesis/ui';
import { AboutModal, AboutModalContent } from '@syndesis/ui';
import { WithRouter } from '@syndesis/utils';
import { useState } from 'react';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { Link, Route, Switch } from 'react-router-dom';
import resolvers from '../modules/resolvers';
import { PageNotFound, WithErrorBoundary } from '../shared';
import { IAppRoute, IAppRoutes, IAppRouteWithChildrens } from './App';
import logo from './syndesis_logo_full_darkbkg.svg';
import { UIContext } from './UIContext';

export interface IAppUIProps {
  routes: IAppRoutes;
}

export interface IAppUIState {
  showAboutModal: boolean;
}

export const UI: React.FunctionComponent<IAppUIProps> = ({ routes }) => {
  const [showNavigation, setShowNavigation] = useState(true);
  const onHideNavigation = () => setShowNavigation(false);
  const onShowNavigation = () => setShowNavigation(true);

  const [showAboutModal, setShowAboutModal] = useState(false);
  const toggleAboutModal = () => {
    setShowAboutModal(!showAboutModal);
  };

  const [notifications, setNotifications] = useState<INotification[]>([]);
  const pushNotification = (msg: string, type: INotificationType) => {
    setNotifications([
      ...notifications,
      {
        key: Date.now().toString(),
        message: msg,
        type,
      },
    ]);
  };
  const onRemoveNotification = (notification: INotification) => {
    setNotifications(
      notifications.filter((n: INotification) => n.key !== notification.key)
    );
  };

  return (
    <UIContext.Provider
      value={{
        hideNavigation: onHideNavigation,
        pushNotification,
        showNavigation: onShowNavigation,
      }}
    >
      <Translation ns={['app', 'shared']}>
        {t => {
          const productName = 'Syndesis';
          return (
            <>
              {showAboutModal && (
                <AboutModal
                  trademark={''}
                  productName={productName}
                  isModalOpen={showAboutModal}
                  handleModalToggle={toggleAboutModal}
                  brandImg={'https://avatars0.githubusercontent.com/u/23079786'}
                >
                  <WithApiVersion>
                    {({ data }) => {
                      const {
                        'commit-id': commitId,
                        'build-id': buildId,
                        version,
                      } = data;
                      return (
                        <AboutModalContent
                          version={version}
                          buildId={buildId}
                          commitId={commitId}
                          productName={productName}
                        />
                      );
                    }}
                  </WithApiVersion>
                </AboutModal>
              )}
              <Notifications
                notifications={notifications}
                notificationTimerDelay={8000}
                removeNotificationAction={onRemoveNotification}
              />
              <WithRouter>
                {({ history, match }) => {
                  return (
                    <AppLayout
                      onShowAboutModal={toggleAboutModal}
                      onSelectSupport={() => {
                        history.push(resolvers.support.root());
                      }}
                      onSelectSampleIntegrationTutorials={() => {
                        window.open(
                          'https://access.redhat.com/documentation/en-us/red_hat_fuse/7.3/html-single/fuse_online_sample_integration_tutorials/',
                          '_blank'
                        );
                      }}
                      onSelectUserGuide={() => {
                        window.open(
                          'https://access.redhat.com/documentation/en-us/red_hat_fuse/7.3/html-single/integrating_applications_with_fuse_online',
                          '_blank'
                        );
                      }}
                      onSelectConnectorsGuide={() => {
                        window.open(
                          'https://access.redhat.com/documentation/en-us/red_hat_fuse/7.3/html-single/connecting_fuse_online_to_applications_and_services/',
                          '_blank'
                        );
                      }}
                      onSelectContactUs={() => {
                        window.location.href =
                          'mailto:fuse-online-tech-preview@redhat.com';
                      }}
                      appNav={
                        <AppTopMenu username={'developer'}>
                          <PfDropdownItem>
                            <Link
                              to={'/logout'}
                              className="pf-c-dropdown__menu-item"
                              children={t('Logout')}
                            />
                          </PfDropdownItem>
                        </AppTopMenu>
                      }
                      verticalNav={routes.map((route, index) =>
                        !(route as IAppRouteWithChildrens).childrens ? (
                          <PfVerticalNavItem
                            exact={(route as IAppRoute).exact}
                            label={t((route as IAppRoute).label)}
                            to={(route as IAppRoute).to}
                            key={index}
                            data-testid={`navbar-link-${
                              (route as IAppRoute).to
                            }`}
                          />
                        ) : (
                          <PfVerticalNavItem
                            label={t(route.label)}
                            key={index}
                            to={'#'}
                          >
                            {(route as IAppRouteWithChildrens).childrens.map(
                              (subRoute, subIndex) => (
                                <PfVerticalNavItem
                                  exact={subRoute.exact}
                                  label={t(subRoute.label)}
                                  to={subRoute.to}
                                  key={subIndex}
                                  data-testid={`navbar-link-${subRoute.to}`}
                                />
                              )
                            )}
                          </PfVerticalNavItem>
                        )
                      )}
                      pictograph={
                        <img
                          src={logo}
                          alt="Syndesis"
                          style={{ minWidth: '164px' }}
                        />
                      }
                      logoHref={'/'}
                      showNavigation={showNavigation}
                      onNavigationCollapse={onHideNavigation}
                      onNavigationExpand={onShowNavigation}
                    >
                      <WithErrorBoundary key={match.url}>
                        <Switch>
                          {routes
                            .reduce(
                              (flattenedRoutes, route) => [
                                ...flattenedRoutes,
                                ...(!(route as IAppRouteWithChildrens).childrens
                                  ? [route as IAppRoute]
                                  : (route as IAppRouteWithChildrens)
                                      .childrens),
                              ],
                              [] as IAppRoute[]
                            )
                            .map((route, index) => (
                              <Route
                                path={route.to}
                                exact={route.exact}
                                component={route.component}
                                key={index}
                              />
                            ))}
                          <Route component={PageNotFound} />
                        </Switch>
                      </WithErrorBoundary>
                    </AppLayout>
                  );
                }}
              </WithRouter>
            </>
          );
        }}
      </Translation>
    </UIContext.Provider>
  );
};
