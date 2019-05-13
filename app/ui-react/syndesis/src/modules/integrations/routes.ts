/* tslint:disable:object-literal-sort-keys */
import { include } from 'named-urls';

export const stepRoutes = {
  // step 1
  selectStep: '',
  // if selected step is api provider
  apiProvider: include('api-provider', {
    upload: '',
    review: 'review',
    edit: 'edit',
  }),
  // if selected step kind is data mapper
  dataMapper: 'mapper',
  // if selected step kind is basic filter
  basicFilter: 'filter',
  // if selected step kind is template
  template: 'template',
  // if selected step kind is step
  step: 'step',
  // if selected step kind is step
  extension: 'extension',
  // if selected step kind is endpoint
  connection: include('connection/:connectionId', {
    selectAction: '',
    configureAction: ':actionId/:step',
    // if 'any' data shape
    describeData: 'describe-data/:position/:direction(input|output)',
  }),
};

/**
 * Both the integration creator and editor share the same routes when the creator
 * reaches the third step in the wizard. This object is to keep them DRY.
 */
const editorRoutes = include(':flowId', {
  index: 'add-step',
  addStep: include(':position/add', stepRoutes),
  editStep: include(':position/edit', stepRoutes),
  saveAndPublish: 'save',
  root: '',
});

export default include('/integrations', {
  list: '',
  manageCicd: include('manageCicd', { root: '' }),
  import: 'import',
  create: include('create', {
    start: include('start/:flowId/:position', stepRoutes),
    finish: include('finish/:flowId/:position', stepRoutes),
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
