import { render, waitForElement } from 'react-testing-library';
import * as React from 'react';
import { I18nextProvider } from 'react-i18next';
import { MemoryRouter } from 'react-router';
import { App } from './App';
import i18n from '../i18n';

jest.unmock('./App');
jest.mock('./WithConfig');

export default describe('App', () => {
  const TestRoute = () => (
    <div data-testid="test-route-container">it renders</div>
  );

  const UnmatchedRoute = () => (
    <div data-testid="test-unmatched-route-container">it DOESN'T renders</div>
  );

  const testComponent = (
    <MemoryRouter initialEntries={['/some-test-url']} initialIndex={1}>
      <I18nextProvider i18n={i18n}>
        <App
          routes={[
            {
              component: TestRoute,
              kind: 'route',
              label: 'Test route',
              to: '/some-test-url',
            },
            {
              component: UnmatchedRoute,
              kind: 'route',
              label: 'Test unmatched route',
              to: '/unmatched',
            },
          ]}
        />
      </I18nextProvider>
    </MemoryRouter>
  );

  it('Should render', async () => {
    const { getByText, getByTestId, queryByTestId } = render(testComponent);

    expect(getByText('Test route')).toBeTruthy();

    const content = await waitForElement(() =>
      getByTestId('test-route-container')
    );
    expect(content).toHaveTextContent('it renders');

    expect(queryByTestId('test-unmatched-route-container')).toBeNull();
  });
});
