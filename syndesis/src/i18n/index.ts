import i18n, { InitOptions } from 'i18next';
import LanguageDetector from 'i18next-browser-languagedetector';
import { en, it } from './locales';

const options = {
  interpolation: {
    escapeValue: false, // not needed for react!!
  },

  debug: false,

  resources: {
    en: {
      app: en.en.app,
      connections: en.en.modules.connections,
      dashboard: en.en.modules.dashboard,
      integrations: en.en.modules.integrations,
      shared: en.en.shared,
    },
    it: {
      app: it.it.app,
      connections: it.it.modules.connections,
      dashboard: it.it.modules.dashboard,
      integrations: it.it.modules.integrations,
      shared: it.it.shared,
    },
  },

  defaultNS: 'shared',
  fallbackLng: process.env.NODE_ENV === 'production' ? 'en' : 'it',
  fallbackNS: ['shared'],
  keySeparator: false, // we do not use keys in form messages.welcome
  ns: ['shared', 'app', 'dashboard'],
} as InitOptions;

i18n.use(LanguageDetector).init(options);
export default i18n;
