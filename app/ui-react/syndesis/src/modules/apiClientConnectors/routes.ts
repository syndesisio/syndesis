import { include } from 'named-urls';

export default include('/api-connector', {
  apiConnector: include(':apiConnectorId', {
    details: '',
    edit: 'edit',
  }),
  create: include('create/swagger-connector', {
    review: 'review',
    root: '',
    save: 'save',
    security: 'security',
    servicePort: 'servicePort',
    specification: 'specification',
    upload: 'upload',
  }),
  list: '',
});
