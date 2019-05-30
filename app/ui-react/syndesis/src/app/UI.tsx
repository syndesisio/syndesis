import { WithApiVersion, WithUserHelpers } from '@syndesis/api';
import {
  AppLayout,
  AppTopMenu,
  ButtonLink,
  INotification,
  INotificationType,
  Notifications,
  PfDropdownItem,
  PfVerticalNavItem,
} from '@syndesis/ui';
import {
  AboutModal,
  AboutModalContent,
  Loader,
  toValidHtmlId,
} from '@syndesis/ui';
import { WithLoader, WithRouter } from '@syndesis/utils';
import { useState } from 'react';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { Route, Switch } from 'react-router-dom';
import { Workbox } from 'workbox-window';
import resolvers from '../modules/resolvers';
import { ApiError, PageNotFound, WithErrorBoundary } from '../shared';
import fuseOnlineLogo from '../shared/images/FuseOnlineLogo_White.svg';
import brandImg from '../shared/images/pf4-downstream-bg.svg';
import syndesisLogoGraphic from '../shared/images/syndesis-logo-graphic.png';
import syndesisLogo from '../shared/images/syndesis_logo_full_darkbkg.svg';
import { IAppRoute, IAppRoutes, IAppRouteWithChildrens } from './App';
import { AppContext } from './AppContext';
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
  const pushNotification = (
    msg: React.ReactNode,
    type: INotificationType,
    persistent: boolean = false
  ) => {
    setNotifications([
      ...notifications,
      {
        key: Date.now().toString(),
        message: msg,
        persistent,
        type,
      },
    ]);
  };
  const onRemoveNotification = (notification: INotification) => {
    setNotifications(
      notifications.filter((n: INotification) => n.key !== notification.key)
    );
  };

  React.useEffect(() => {
    let refreshNotificationDisplayed = false;
    // tslint:disable
    if ('serviceWorker' in navigator) {
      const wb = new Workbox('/service-worker.js');

      const refreshApp = () => {
        wb.addEventListener('controlling', event => {
          window.location.reload();
        });
        // Send a message telling the service worker to skip waiting.
        // This will trigger the `controlling` event handler above.
        wb.messageSW({ type: 'SKIP_WAITING' });
      };

      wb.addEventListener('waiting', event => {
        if (!refreshNotificationDisplayed) {
          refreshNotificationDisplayed = true;
          pushNotification(
            <>
              <div className="pull-right toast-pf-action">
                <ButtonLink
                  data-testid={'ui-reload-button'}
                  onClick={refreshApp}
                  as={'link'}
                  style={{ padding: 0, border: 0 }}
                >
                  Reload
                </ButtonLink>
              </div>
              A new version is available.
            </>,
            'warning',
            true
          );
        }
      });

      wb.register();
    }
  }, []); // eslint-disable-line

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
          return (
            <AppContext.Consumer>
              {({ user, config }) => {
                const isProductBuild = config && config.branding.productBuild;
                const productName = isProductBuild ? 'Fuse Online' : 'Syndesis';
                return (
                  <>
                    {showAboutModal && (
                      <AboutModal
                        trademark={''}
                        productName={productName}
                        isModalOpen={showAboutModal}
                        handleModalToggle={toggleAboutModal}
                        bgImg={isProductBuild ? brandImg : undefined}
                        brandImg={
                          isProductBuild ? fuseOnlineLogo : syndesisLogoGraphic
                        }
                      >
                        <WithApiVersion>
                          {({ data, error, loading }) => {
                            const {
                              'commit-id': commitId,
                              'build-id': buildId,
                              version,
                            } = data;
                            return (
                              <WithLoader
                                error={error}
                                loading={loading}
                                errorChildren={<ApiError />}
                                loaderChildren={<Loader />}
                              >
                                {() => (
                                  <AboutModalContent
                                    version={version}
                                    buildId={buildId}
                                    commitId={commitId}
                                    productName={productName}
                                  />
                                )}
                              </WithLoader>
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
                              <WithUserHelpers>
                                {({ logout }) => {
                                  return (
                                    <AppTopMenu
                                      username={user.username || 'developer'}
                                    >
                                      <PfDropdownItem onClick={logout}>
                                        <button
                                          type="button"
                                          role="menuitem"
                                          data-testid={'ui-logout-link'}
                                          className="pf-c-dropdown__menu-item"
                                        >
                                          {t('Logout')}
                                        </button>
                                      </PfDropdownItem>
                                    </AppTopMenu>
                                  );
                                }}
                              </WithUserHelpers>
                            }
                            verticalNav={routes.map((route, index) =>
                              !(route as IAppRouteWithChildrens).childrens ? (
                                <PfVerticalNavItem
                                  exact={(route as IAppRoute).exact}
                                  label={t((route as IAppRoute).label)}
                                  to={(route as IAppRoute).to}
                                  key={index}
                                  data-testid={`ui-${toValidHtmlId(
                                    (route as IAppRoute).label
                                  )}`}
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
                                        data-testid={`ui-${toValidHtmlId(
                                          subRoute.label
                                        )}`}
                                      />
                                    )
                                  )}
                                </PfVerticalNavItem>
                              )
                            )}
                            pictograph={
                              <img
                                src={
                                  isProductBuild ? fuseOnlineLogo : syndesisLogo
                                }
                                alt={productName}
                                style={{
                                  minWidth: '164px',
                                }}
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
                                      ...(!(route as IAppRouteWithChildrens)
                                        .childrens
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
            </AppContext.Consumer>
          );
        }}
      </Translation>
    </UIContext.Provider>
  );
};
