// The file contents for the current environment will overwrite these during build.
// The build system defaults to the dev environment which uses `environment.ts`, but if you do
// `ng build --env=prod` then `environment.prod.ts` will be used instead.
// The list of which env maps to which file can be found in `angular-cli.json`.

export const environment = Object.freeze({
  production: false,
  config: {
    apiEndpoint: 'http://localhost:8080/api/v1',
    title: 'DEVELOPMENT - Red Hat iPaaS',
    oauth: {
      clientId: 'ipaas-ui',
      scopes: [],
      oidc: true,
      hybrid: true,
      issuer: 'http://localhost:8282/auth/realms/ipaas-test',
    },
  },
});
