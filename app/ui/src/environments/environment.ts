// The file contents for the current environment will overwrite these during build.
// The build system defaults to the dev environment which uses `environment.ts`, but if you do
// `ng build --env=prod` then `environment.prod.ts` will be used instead.
// The list of which env maps to which file can be found in `angular-cli.json`.

export const environment = Object.freeze({
  production: false,
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
  config: {
    apiEndpoint: 'http://localhost:8080/api/v1',
    title: 'DEVELOPMENT - Syndesis',
    datamapper: {
      baseJavaInspectionServiceUrl: 'http://localhost:8585/v2/atlas/java/',
      baseXMLInspectionServiceUrl: 'http://localhost:8585/v2/atlas/xml/',
      baseJSONInspectionServiceUrl: 'http://localhost:8585/v2/atlas/json/',
      baseMappingServiceUrl: 'http://localhost:8585/v2/atlas/'
    }
  }
});
