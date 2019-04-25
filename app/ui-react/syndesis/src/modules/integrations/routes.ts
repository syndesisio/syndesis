/* tslint:disable:object-literal-sort-keys */
import { include } from 'named-urls';

const stepRoutes = {
  // step 1
  selectStep: '',
  // if selected step is api provider
  apiProvider: include('api-provider', {
    specification: 'specification',
    review: 'review',
    edit: 'edit',
    save: 'save',
  }),
  // if selected step kind is data mapper
  dataMapper: 'mapper',
  // if selected step kind is basic filter
  basicFilter: 'filter',
  // if selected step kind is template
  template: 'template',
  // if selected step kind is step
  step: 'step',
  // if selected step kind is endpoint
  connection: include('connection/:connectionId', {
    selectAction: 'action',
    configureAction: 'action/:actionId/step/:step',
    // if 'any' data shape
    describeData: 'describe-data/:position/:direction(input|output)',
  }),
};

/**
 * Both the integration creator and editor share the same routes when the creator
 * reaches the third step in the wizard. This object is to keep them DRY.
 */
const editorRoutes = include('flow/:flow', {
  index: 'add-step',
  addStep: include('position/:position/connection', stepRoutes),
  editStep: include('position/:position/edit-connection', stepRoutes),
  saveAndPublish: 'save',
  root: '',
});

export default include('/integrations', {
  list: '',
  manageCicd: include('manageCicd', { root: '' }),
  import: include('import', { root: '' }),
  create: include('create', {
    start: include('start/flow/:flow/position/:position', stepRoutes),
    finish: include('finish/flow/:flow/position/:position', stepRoutes),
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
