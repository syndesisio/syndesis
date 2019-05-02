import {
  AppLayout,
  AppTopMenu,
  INotification,
  INotificationType,
  Notifications,
  PfDropdownItem,
  PfVerticalNavItem,
} from '@syndesis/ui';
import { WithRouter } from '@syndesis/utils';
import { useState } from 'react';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { Link, Route, Switch } from 'react-router-dom';
import { PageNotFound, WithErrorBoundary } from '../shared';
import { IAppRoute } from './App';
import logo from './syndesis_logo_full_darkbkg.svg';
import { UIContext } from './UIContext';

export interface IAppUIProps {
  routes: IAppRoute[];
}

export const UI: React.FunctionComponent<IAppUIProps> = ({ routes }) => {
  const [showNavigation, setShowNavigation] = useState(true);
  const onHideNavigation = () => setShowNavigation(false);
  const onShowNavigation = () => setShowNavigation(true);

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
        {t => (
          <AppLayout
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
            verticalNav={routes.map(({ exact, icon, label, to }, index) => (
              <PfVerticalNavItem
                exact={exact}
                icon={icon}
                label={t(label)}
                to={to}
                key={index}
                data-testid={`navbar-link-${to}`}
              />
            ))}
            pictograph={
              <img src={logo} alt="Syndesis" style={{ minWidth: '164px' }} />
            }
            logoHref={'/'}
            showNavigation={showNavigation}
            onNavigationCollapse={onHideNavigation}
            onNavigationExpand={onShowNavigation}
          >
            <Notifications
              notifications={notifications}
              notificationTimerDelay={8000}
              removeNotificationAction={onRemoveNotification}
            />
            <WithRouter>
              {({ match }) => (
                <WithErrorBoundary key={match.url}>
                  <Switch>
                    {routes.map(({ to, exact, component }, index) => (
                      <Route
                        path={to}
                        exact={exact}
                        component={component}
                        key={index}
                      />
                    ))}
                    <Route component={PageNotFound} />
                  </Switch>
                </WithErrorBoundary>
              )}
            </WithRouter>
          </AppLayout>
        )}
      </Translation>
    </UIContext.Provider>
  );
};
