import { render } from 'react-testing-library';
import * as React from 'react';
import { I18nextProvider } from 'react-i18next';
import { App } from './App';
import i18n from '../i18n';

export default describe('App', () => {
  const testComponent = (
    <I18nextProvider i18n={i18n}>
      <App />
    </I18nextProvider>
  );

  it('Should render', () => {
    const { container } = render(testComponent);
    expect(container).toBeDefined();
  });
});
