import { action } from '@storybook/addon-actions';
import { text } from '@storybook/addon-knobs';
import { MemoryRouter } from 'react-router';
import { storiesOf } from '@storybook/react';
import * as React from 'react';
import {
  AboutModal,
  AboutModalContent,
  AppLayout,
  AppTopMenu,
  PfDropdownItem,
  PfVerticalNavItem,
} from '../../src';
import { withState } from '@dump247/storybook-state';
const stories = storiesOf('Layout/AppLayout', module);
const logDropdownItemSelection = action('select dropdown item log');
const logLogout = action('logout action');

stories.add(
  'sample usage',
  withState({ showAboutModal: false })(({ store }) => {
    function toggleAboutModal() {
      store.set({
        showAboutModal: !store.state.showAboutModal,
      });
    }
    return (
      <div className={'layout-pf layout-pf-fixed'}>
        <MemoryRouter initialEntries={['/test']}>
          <>
            <AboutModal
              bgImg={undefined}
              trademark={'Red Hat'}
              productName="Syndesis"
              isModalOpen={store.state.showAboutModal}
              handleModalToggle={toggleAboutModal}
              brandImg={'https://avatars0.githubusercontent.com/u/23079786'}
            >
              <AboutModalContent
                productName="Syndesis"
                version={'1.7-SNAPSHOT'}
                commitId={'dd8b5445fd74f956147eb0a21870d0d5c3e0fb69'}
                buildId={'60dfad7e-fba5-49e9-b393-e806a135299e'}
              />
            </AboutModal>

            <AppLayout
              onNavigationCollapse={() => null}
              onNavigationExpand={() => null}
              onSelectConnectorsGuide={logDropdownItemSelection}
              onSelectContactUs={logDropdownItemSelection}
              onSelectSampleIntegrationTutorials={logDropdownItemSelection}
              onSelectSupport={logDropdownItemSelection}
              onSelectUserGuide={logDropdownItemSelection}
              onShowAboutModal={() => {
                logDropdownItemSelection();
                toggleAboutModal();
              }}
              showNavigation={true}
              pictograph={text('Application title', 'Syndesis')}
              appNav={
                <AppTopMenu username={'developer'}>
                  <PfDropdownItem
                    onClick={event => {
                      event && event.preventDefault();
                      logLogout();
                    }}
                  >
                    Logout
                  </PfDropdownItem>
                </AppTopMenu>
              }
              verticalNav={[
                <PfVerticalNavItem
                  exact={true}
                  label={'Homepage'}
                  to={'#test'}
                  key={1}
                />,
                <PfVerticalNavItem
                  exact={true}
                  label={'Sample'}
                  to={'#test2'}
                  key={2}
                />,
              ]}
              logoHref={'#test'}
            />
          </>
        </MemoryRouter>
      </div>
    );
  })
);
