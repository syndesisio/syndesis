import * as React from 'react';
import { MemoryRouter } from 'react-router';
import { render } from 'react-testing-library';
import { AppLayout, PfVerticalNavItem } from '../src/';

export default describe('ConnectionCard', () => {
  const modalHandler = jest.fn();
  const selectSupportHandler = jest.fn();
  const selectSampleIntegrationTutorialsHandler = jest.fn();
  const selectUserGuideHandler = jest.fn();
  const selectConnectorsGuideHandler = jest.fn();
  const selectContactUsHandler = jest.fn();
  const logout = jest.fn();
  const testComponent = (
    <MemoryRouter>
      <AppLayout
        logoutItem={{
          key: 'logoutMenuItem',
          onClick: logout,
          id: 'ui-logout-link',
          className: 'pf-c-dropdown__menu-item',
          children: 'Logout',
        }}
        username={'developer'}
        onSelectSupport={selectSupportHandler}
        onSelectSampleIntegrationTutorials={
          selectSampleIntegrationTutorialsHandler
        }
        onSelectUserGuide={selectUserGuideHandler}
        onSelectConnectorsGuide={selectConnectorsGuideHandler}
        onSelectContactUs={selectContactUsHandler}
        pictograph={'Syndesis'}
        onShowAboutModal={modalHandler}
        verticalNav={[
          <PfVerticalNavItem
            exact={true}
            label={'Homepage'}
            to={'#navlink'}
            key={1}
            data-testid={'navlink'}
          />,
        ]}
        logoOnClick={() => false}
        showNavigation={false}
        onNavigationCollapse={() => true}
        onNavigationExpand={() => true}
      />
    </MemoryRouter>
  );

  it('should render a top menu and help dropdown', () => {
    const { getByTestId } = render(testComponent);
    expect(getByTestId('appTopMenu')).toBeTruthy();
    expect(getByTestId('helpDropdownButton')).toBeTruthy();
  });

  it('vertical navigation items should render', () => {
    const { getByTestId, getByText } = render(testComponent);
    expect(getByTestId('navlink')).toBeTruthy();
    expect(getByText('Homepage')).toBeTruthy();
  });
});
