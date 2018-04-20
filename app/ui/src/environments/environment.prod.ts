export const environment = {
  production: true,
  i18n: {
    fallbackValue: '?',
    localStorageKey: 'syndesis-i18n-locale',
    dictionaryFolderPath: '/assets/dictionary'
  },
  xsrf: {
    headerName: 'SYNDESIS-XSRF-TOKEN',
    cookieName: 'SYNDESIS-XSRF-COOKIE',
    defaultTokenValue: 'awesome'
  },
  config: {}
};
