
import {
  AboutModal,
  AboutModalContent,
  AppLayout,
  INotification,
  INotificationType,
  Loader,
  Notifications,
  PfVerticalNavItem,
  toValidHtmlId,
} from '@syndesis/ui';
import { IAppRoute, IAppRoutes, IAppRouteWithChildrens } from './App';
import { WithApiVersion, WithUserHelpers } from '@syndesis/api';
import { StringMap } from '@syndesis/models';
import { WithLoader, WithRouter } from '@syndesis/utils';
import * as React from 'react';
import * as ReactDOM from 'react-dom';
import { Translation } from 'react-i18next';
import { Route, Switch } from 'react-router-dom';
import resolvers from '../modules/resolvers';
import { ApiError, PageNotFound, WithErrorBoundary } from '../shared';
import favicon from '../shared/images/favicon.ico';
import brandImg from '../shared/images/pf4-downstream-bg.svg';
import redHatBrandLogo from '../shared/images/red-hat-brand-logo.png';
import redHatFuseOnlineLogo from '../shared/images/red-hat-fuse-online-logo.png';
import synAppleTouchIcon from '../shared/images/syn-apple-touch-icon.png';
import syndesisLogoGraphic from '../shared/images/syndesis-logo-graphic.png';
import syndesisLogo from '../shared/images/syndesis_logo_full_darkbkg.svg';
import rhAppleTouchIcon from '../shared/images/red-hat-apple-touch-icon.png'; // tslint:disable-line
import redHatSafariPinnedTabIcon from '../shared/images/red-hat-safari-pinned-tab.svg';
import synFavicon from '../shared/images/syn-favicon.ico';
import synSafariPinnedTabIcon from '../shared/images/syn-safari-pinned-tab.svg';
import { AppContext } from './AppContext';
import { UIContext } from './UIContext';

export interface IAppUIProps {
  routes: IAppRoutes;
}

export interface IAppUIState {
  showAboutModal: boolean;
}

/**
 * Convert a route to a feature name based on it's path, returns
 * undefined if the route doesn't have a path.
 * @param route
 */
function routeToFeatureName(route: IAppRoute) {
  if (!route.to) {
    return undefined;
  }
  return route.to.replace(/\//g, '').trim();
}

/**
 * Compares the supplied route to the given feature map and
 * returns whether or not that module should be visible to the
 * user or not
 * @param features
 * @param route
 */
function featureEnabled(features: StringMap<any>, route: IAppRoute) {
  const feature = routeToFeatureName(route);
  if (typeof feature === 'undefined') {
    return true;
  }
  return (
    typeof features[feature] === 'undefined' ||
    (typeof features[feature] === 'boolean' && features[feature])
  );
}

export const UI: React.FunctionComponent<IAppUIProps> = ({ routes }) => {
  const [showNavigation, setShowNavigation] = React.useState(true);
  const onHideNavigation = () => setShowNavigation(false);
  const onShowNavigation = () => setShowNavigation(true);

  const [showAboutModal, setShowAboutModal] = React.useState(false);
  const toggleAboutModal = () => {
    setShowAboutModal(!showAboutModal);
  };

  const [notifications, setNotifications] = React.useState<INotification[]>([]);
  const removeNotification = (toRemove: string) => {
    setNotifications(items => items.filter(({ key }) => key !== toRemove));
  };
  const pushNotification = (
    message: React.ReactNode,
    type: INotificationType,
    persistent: boolean = false
  ) => {
    setNotifications([
      ...notifications,
      {
        key: Date.now().toString(),
        message,
        persistent,
        type,
      },
    ]);
  };
  /* disable listening to the web worker to avoid installing it
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
  */

  const updateHref = (id: string, assetUrl: string) => {
    const element =
      document.getElementById(id) || document.createElement('link');
    element.setAttribute('href', assetUrl);
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
          return (
            <AppContext.Consumer>
              {({ user, config }) => {
                const isProductBuild = config && config.branding.productBuild;
                updateHref('favicon', !isProductBuild ? synFavicon : favicon);
                updateHref(
                  'appleTouchIcon',
                  !isProductBuild ? synAppleTouchIcon : rhAppleTouchIcon
                );
                updateHref(
                  'safariPinnedTab',
                  !isProductBuild ? synSafariPinnedTabIcon : redHatSafariPinnedTabIcon
                );
                const productName = t('shared:project:name');
                const features =
                  (config && config.features) || ({} as Map<string, any>);
                return (
                  <>
                    {
                      <AboutModal
                        trademark={''}
                        productName={productName}
                        isModalOpen={showAboutModal}
                        handleModalToggle={toggleAboutModal}
                        bgImg={isProductBuild ? brandImg : undefined}
                        brandImg={
                          isProductBuild ? redHatBrandLogo : syndesisLogoGraphic
                        }
                      >
                        <WithApiVersion>
                          {({ data, error, errorMessage, loading }) => {
                            const {
                              'commit-id': commitId,
                              'build-id': buildId,
                              version,
                            } = data;
                            return (
                              <WithLoader
                                error={error}
                                loading={loading}
                                errorChildren={
                                  <ApiError error={errorMessage!} />
                                }
                                loaderChildren={<Loader />}
                              >
                                {() => (
                                  <AboutModalContent
                                    version={version}
                                    buildId={buildId}
                                    commitId={commitId}
                                    productName={productName}
                                    i18nBuildIdLabel={t('BuildIdLabel')}
                                    i18nCommitIdLabel={t('CommitIdLabel')}
                                  />
                                )}
                              </WithLoader>
                            );
                          }}
                        </WithApiVersion>
                      </AboutModal>
                    }
                    <Notifications
                      notifications={notifications}
                      onClose={removeNotification}
                    />
                    <WithRouter>
                      {({ history, match }) => {
                        return (
                          <WithUserHelpers>
                            {({ logout }) => {
                              const doLogout = async () => {
                                ReactDOM.unmountComponentAtNode(
                                  document.getElementById('root') as HTMLElement
                                );
                                const buffer = await logout();
                                const bytes = new Uint8Array(buffer);
                                const decoder = new TextDecoder('utf-8');
                                const html = decoder.decode(bytes);
                                window.history.pushState(null, '', '/logout');
                                window.document.open();
                                window.document.write(html);
                                window.document.close();
                              };
                              const tutorialLink = t('shared:links:tutorial');
                              const userGuideLink = t('shared:links:userguide');
                              const connectorsGuideLink = t(
                                'shared:links:connectorsguide'
                              );
                              const contactUsLink = t('shared:links:contactus');
                              return (
                                <AppLayout
                                  onShowAboutModal={toggleAboutModal}
                                  onSelectSupport={() => {
                                    history.push(resolvers.support.root());
                                  }}
                                  onSelectSampleIntegrationTutorials={() => {
                                    window.open(tutorialLink, '_blank');
                                  }}
                                  onSelectUserGuide={() => {
                                    window.open(userGuideLink, '_blank');
                                  }}
                                  onSelectConnectorsGuide={() => {
                                    window.open(connectorsGuideLink, '_blank');
                                  }}
                                  onSelectContactUs={() => {
                                    window.open(contactUsLink, '_blank');
                                  }}
                                  logoutItem={{
                                    children: t('Logout'),
                                    className: 'pf-c-dropdown__menu-item',
                                    id: 'ui-logout-link',
                                    key: 'logoutMenuItem',
                                    onClick: doLogout,
                                  }}
                                  username={user.username || 'developer'}
                                  verticalNav={routes.map((route, index) =>
                                    !(route as IAppRouteWithChildrens)
                                      .childrens ? (
                                      <PfVerticalNavItem
                                        hidden={
                                          !featureEnabled(
                                            features,
                                            route as IAppRoute
                                          )
                                        }
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
                                              hidden={
                                                !featureEnabled(
                                                  features,
                                                  subRoute as IAppRoute
                                                )
                                              }
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
                                        isProductBuild
                                          ? redHatFuseOnlineLogo
                                          : syndesisLogo
                                      }
                                      alt={productName}
                                      className="pf-c-brand"
                                    />
                                  }
                                  logoOnClick={() => history.push('/')}
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
                          </WithUserHelpers>
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
