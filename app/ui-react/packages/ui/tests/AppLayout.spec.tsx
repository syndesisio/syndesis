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
  const testComponent = (
    <MemoryRouter>
      <AppLayout
        onSelectSupport={selectSupportHandler}
        onSelectSampleIntegrationTutorials={
          selectSampleIntegrationTutorialsHandler
        }
        onSelectUserGuide={selectUserGuideHandler}
        onSelectConnectorsGuide={selectConnectorsGuideHandler}
        onSelectContactUs={selectContactUsHandler}
        pictograph={'Syndesis'}
        onShowAboutModal={modalHandler}
        appNav={<div data-testid="appnav">appnav</div>}
        verticalNav={[
          <PfVerticalNavItem
            exact={true}
            label={'Homepage'}
            to={'#navlink'}
            key={1}
            data-testid={'navlink'}
          />,
        ]}
        logoHref={'#test'}
        showNavigation={false}
        onNavigationCollapse={() => true}
        onNavigationExpand={() => true}
      />
    </MemoryRouter>
  );

  it('app navigation items should render', () => {
    const { getByTestId } = render(testComponent);
    expect(getByTestId('appnav')).toBeTruthy();
  });

  it('vertical navigation items should render', () => {
    const { getByTestId, getByText } = render(testComponent);
    expect(getByTestId('navlink')).toBeTruthy();
    expect(getByText('Homepage')).toBeTruthy();
  });
});
