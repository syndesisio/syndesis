import * as React from 'react';
import { MemoryRouter } from 'react-router';
import { render } from 'react-testing-library';
import { AppLayout, PfVerticalNavItem } from '../src/';

export default describe('ConnectionCard', () => {
  const modalHandler = jest.fn();
  const testComponent = (
    <MemoryRouter>
      <AppLayout
        pictograph={'Syndesis'}
        onShowAboutModal={modalHandler}
        appNav={<div data-testid="appnav">appnav</div>}
        verticalNav={[
          <PfVerticalNavItem
            exact={true}
            icon={'home'}
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
