/* tslint:disable:object-literal-sort-keys */
import { include } from 'named-urls';

const editorRoutes = {
  index: 'save-or-add-step',
  addConnection: include(':position/connection', {
    selectConnection: '',
    selectAction: `:connectionId`,
    configureAction: `:connectionId/:actionId/:step?`,
  }),
  editConnection: include(':position/edit-connection', {
    selectAction: `select-action/:connectionId`,
    configureAction: `:actionId/:step?`,
  }),
  addStep: include(':position/step', {
    selectStep: '',
    configureStep: `:stepId`,
  }),
  editStep: ':position/edit-step',
  saveAsDraft: 'save-as-draft',
};

export default include('/integrations', {
  list: '',
  create: include('create', {
    start: include('start', {
      selectConnection: '',
      selectAction: `:connectionId`,
      configureAction: `:connectionId/:actionId/:step?`,
    }),
    finish: include('finish', {
      selectConnection: ``,
      selectAction: `:connectionId`,
      configureAction: `:connectionId/:actionId/:step?`,
    }),
    configure: include('configure', editorRoutes),
    root: '',
  }),
  integration: include(':integrationId', {
    details: '',
    edit: include('edit', editorRoutes),
  }),
});
