import i18n, { InitOptions } from 'i18next';
import LanguageDetector from 'i18next-browser-languagedetector';
import { en, it, ru } from './locales';

const options: InitOptions = {
  interpolation: {
    escapeValue: false, // not needed for react!!
  },

  debug: false,

  resources: {
    en: {
      apiClientConnectors: en.en.modules.apiClientConnectors,
      app: en.en.app,
      connections: en.en.modules.connections,
      dashboard: en.en.modules.dashboard,
      data: en.en.modules.data,
      extensions: en.en.modules.extensions,
      integrations: en.en.modules.integrations,
      settings: en.en.modules.settings,
      shared: en.en.shared,
    },
    it: {
      apiClientConnectors: it.it.modules.apiClientConnectors,
      app: it.it.app,
      connections: it.it.modules.connections,
      dashboard: it.it.modules.dashboard,
      data: it.it.modules.data,
      extensions: it.it.modules.extensions,
      integrations: it.it.modules.integrations,
      settings: it.it.modules.settings,
      shared: it.it.shared,
    },
    ru: {
      apiClientConnectors: ru.ru.modules.apiClientConnectors,
      app: ru.ru.app,
      connections: ru.ru.modules.connections,
      dashboard: ru.ru.modules.dashboard,
      data: ru.ru.modules.data,
      extensions: ru.ru.modules.extensions,
      integrations: ru.ru.modules.integrations,
      settings: ru.ru.modules.settings,
      shared: ru.ru.shared,
    },
  },

  defaultNS: 'shared',
  fallbackLng: 'en',
  fallbackNS: ['shared'],
  keySeparator: '.',
  ns: [
    'shared',
    'app',
    'apiClientConnectors',
    'connections',
    'dashboard',
    'extensions',
    'integrations',
    'settings',
  ],
};

i18n.use(LanguageDetector).init(options);
export default i18n;
