/* tslint:disable:object-literal-sort-keys */
import { include } from 'named-urls';

const editorRoutes = {
  index: 'save-or-add-step',
  addConnection: include(':position', {
    selectConnection: '',
    selectAction: `:connectionId`,
    configureAction: `:connectionId/:actionId/:step?`,
  }),
  addStep: include(':position', {
    selectStep: '',
    configureStep: `:stepId`,
  }),
  saveAsDraft: 'save-as-draft',
};

export default {
  integrations: include('/integrations', {
    list: '',
    create: include('create', {
      root: '',
      start: include('start', {
        selectConnection: '',
        configureAction: `:connectionId/:actionId/:step?`,
        selectAction: `:connectionId`,
      }),
      finish: include('finish', {
        selectConnection: ``,
        selectAction: `:connectionId`,
        configureAction: `:connectionId/:actionId/:step?`,
      }),
      configure: include('configure', editorRoutes),
    }),
    integration: include(':integrationId', {
      details: '',
      edit: include('edit', editorRoutes),
    }),
  }),
};
