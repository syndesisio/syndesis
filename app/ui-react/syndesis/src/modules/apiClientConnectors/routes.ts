import { include } from 'named-urls';

export default include('/api-connector', {
  apiConnector: include(':apiConnectorId', {
    details: '',
    edit: 'edit',
  }),
  create: include('create/swagger-connector', {
    review: 'review',
    save: 'save',
    security: 'security',
    upload: 'upload',
  }),
  list: '',
});
