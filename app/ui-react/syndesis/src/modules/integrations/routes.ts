/* tslint:disable:object-literal-sort-keys */
import { include } from 'named-urls';
import { editorRoutes, stepRoutes } from './components/editor/interfaces';

export default include('/integrations', {
  list: '',
  manageCicd: include('manageCicd', { root: '' }),
  import: 'import',
  create: include('create', {
    start: include(':integrationId/start/:flowId/:position', stepRoutes),
    finish: include(':integrationId/finish/:flowId/:position', stepRoutes),
    configure: include(':integrationId/configure', editorRoutes),
    root: '',
  }),
  integration: include(':integrationId', {
    details: 'details',
    activity: 'activity',
    metrics: 'metrics',
    edit: include('edit', editorRoutes),
    root: '',
  }),
});
