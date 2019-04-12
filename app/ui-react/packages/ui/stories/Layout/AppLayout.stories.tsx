import { text } from '@storybook/addon-knobs';
import { MemoryRouter } from 'react-router';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import {
  AppLayout,
  AppTopMenu,
  PfDropdownItem,
  PfVerticalNavItem,
} from '../../src';
import { Link } from 'react-router-dom';

const stories = storiesOf('Layout/AppLayout', module);

stories.add('sample usage', () => (
  <div className={'layout-pf layout-pf-fixed'}>
    <MemoryRouter initialEntries={['/test']}>
      <AppLayout
        appTitle={text('Application title', 'Syndesis')}
        onNavigationCollapse={() => null}
        onNavigationExpand={() => null}
        showNavigation={true}
        pictograph={text('Application title', 'Syndesis')}
        appNav={
          <AppTopMenu username={'developer'}>
            <PfDropdownItem>
              <Link
                to={'/logout'}
                className="pf-c-dropdown__menu-item"
                children={'Logout'}
              />
            </PfDropdownItem>
          </AppTopMenu>
        }
        verticalNav={[
          <PfVerticalNavItem
            exact={true}
            icon={'home'}
            label={'Homepage'}
            to={'#test'}
            key={1}
          />,
          <PfVerticalNavItem
            exact={true}
            icon={'dashboard'}
            label={'Sample'}
            to={'#test2'}
            key={2}
          />,
        ]}
        logoHref={'#test'}
      />
    </MemoryRouter>
  </div>
));
