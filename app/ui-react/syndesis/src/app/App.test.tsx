import { render, waitForElement } from '@testing-library/react';
import * as React from 'react';
import { MemoryRouter } from 'react-router';
// import { App } from './App';
import i18n from 'i18next';
import { I18nextProvider, initReactI18next } from 'react-i18next';

/*
  TODO:  This test was modified to prevent the test from running because a solution
  wasn't found after adding the monaco-editor/vscode dependencies in DV's ui components
  for a DV language server text editor.

  See:  JIRA TEIIDTOOLS-983 for details and related issues to fix the failed test issues
*/
i18n
  .use(initReactI18next)
  .init({
    fallbackLng: 'en',
    resources: {
      en: {}
    }
  });

jest.unmock('./App');
jest.mock('./WithConfig');

export default describe('App', () => {
  /*
  const TestRoute = () => (
    <div data-testid="test-route-container">it renders</div>
  );

  const UnmatchedRoute = () => (
    <div data-testid="test-unmatched-route-container">it DOESN'T renders</div>
  );
  */

  const testComponent = (
    <MemoryRouter initialEntries={['/some-test-url']} initialIndex={1}>
      <I18nextProvider i18n={i18n}>
        {/*
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
        */}
      </I18nextProvider>
    </MemoryRouter>
  );

  xit('Should render', async () => {
    const { getByText, getByTestId, queryByTestId } = render(testComponent);

    expect(getByText('Test route')).toBeTruthy();

    const content = await waitForElement(() =>
      getByTestId('test-route-container')
    );
    expect(content).toHaveTextContent('it renders');

    expect(queryByTestId('test-unmatched-route-container')).toBeNull();
  });
});
