/* tslint:disable:object-literal-sort-keys */
import { include } from 'named-urls';

export default {
  integrations: include('/integrations', {
    list: '',
    create: include('create', {
      begin: '',
      start: include('start', {
        configureAction: `:connectionId/:actionId/:step?`,
        selectAction: `:connectionId`,
      }),
      finish: include('finish', {
        configureAction: `:connectionId/:actionId/:step?`,
        selectAction: `:connectionId`,
        selectConnection: ``,
      }),
    }),
    editor: include('edit/:integrationId', {
      begin: '',
    }),
  }),
};
