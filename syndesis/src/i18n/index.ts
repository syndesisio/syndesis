import i18n from 'i18next';
import { en, it } from './locales';

const options = {
  interpolation: {
    escapeValue: false, // not needed for react!!
  },

  debug: true, // change to false for release

  resources: {
    en: {
      app: en.en.app,
      dashboard: en.en.modules.dashboard,
      shared: en.en.shared,
    },
    it: {
      app: it.it.app,
      dashboard: it.it.modules.dashboard,
      shared: it.it.shared,
    },
  },

  defaultNS: 'shared',
  fallbackLng: 'it',
  keySeparator: false, // we do not use keys in form messages.welcome
  ns: ['shared'],

  react: {
    bindI18n: 'languageChanged loaded',
    bindStore: 'added removed',
    nsMode: 'default',
    wait: false,
  },
};

i18n.init(options);
export default i18n;
