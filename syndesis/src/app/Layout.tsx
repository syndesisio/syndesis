import { PfNavLink } from '@syndesis/ui';
import { Icon, Masthead, VerticalNav } from 'patternfly-react';
import * as React from 'react';
import { NamespacesConsumer } from 'react-i18next';
import { RouteComponentProps, withRouter } from 'react-router';

import pitto from './glasses_logo_square.png';
import typo from './syndesis-logo-svg-white.svg';

export interface ILayoutBase extends RouteComponentProps {
  navbar?: any;
}

class LayoutBase extends React.Component<ILayoutBase> {
  public render() {
    return (
      <NamespacesConsumer ns={['app']}>
        {t => (
          <React.Fragment>
            <VerticalNav sessionKey={'mainNav'}>
              <VerticalNav.Masthead
                iconImg={pitto}
                titleImg={typo}
                title="Syndesis"
                href={'/'}
                onTitleClick={this.goToHome}
              >
                <Masthead.Collapse>
                  <Masthead.Dropdown
                    id="app-user-dropdown"
                    title={[
                      <span className="dropdown-title" key="dropdown-title">
                        <Icon type={'fa'} name={'user'} /> developer
                      </span>,
                    ]}
                  >
                    <PfNavLink to={'/logout'} label={t('Logout')} />
                  </Masthead.Dropdown>
                </Masthead.Collapse>
              </VerticalNav.Masthead>
              {this.props.navbar}
            </VerticalNav>
            <div className="container-pf-nav-pf-vertical">
              {this.props.children}
            </div>
          </React.Fragment>
        )}
      </NamespacesConsumer>
    );
  }

  protected goToHome = () => {
    this.props.history.replace('/');
  };
}

export const Layout = withRouter(LayoutBase);
