import { include } from 'named-urls';

export default include('/customizations', {
  apiConnectors: include('api-connector', {
    apiConnector: ':apiConnectorId',
    create: include('create/swagger-connector', {
      review: 'review',
      save: 'save',
      security: 'security',
      upload: 'upload',
    }),
    list: '',
  }),
  extensions: include('extensions', {
    extension: include(':extensionId', {
      details: '',
      update: 'update',
    }),
    import: 'import',
    list: '',
  }),
  root: '',
});
