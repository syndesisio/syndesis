/* tslint:disable:object-literal-sort-keys */
import { include } from 'named-urls';

const stepRoutes = {
  selectConnection: '',
  selectAction: `:connectionId`,
  configureAction: `:connectionId/action::actionId/step::step?`,
};

/**
 * Both the integration creator and editor share the same routes when the creator
 * reaches the third step in the wizard. This object is to keep them DRY.
 */
const editorRoutes = include('flow::flow', {
  index: 'add-step',
  addStep: include('position::position/connection', stepRoutes),
  editStep: include('position::position/edit-connection', stepRoutes),
  saveAndPublish: 'save',
  root: '',
});

export default include('/integrations', {
  list: '',
  manageCicd: include('manageCicd', { root: '' }),
  import: include('import', { root: '' }),
  create: include('create', {
    start: include('start', stepRoutes),
    finish: include('finish', stepRoutes),
    configure: include('configure', editorRoutes),
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
