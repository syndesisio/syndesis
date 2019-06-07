/* tslint:disable:object-literal-sort-keys */
import { include } from 'named-urls';

export default include('/connections', {
  connections: '',
  connection: include(':connectionId', {
    edit: 'edit',
    details: '',
  }),
  create: include('create', {
    selectConnector: 'connection-basics',
    configureConnector: ':connectorId/configure-fields',
    review: ':connectorId/review',
    root: '',
  }),
});
