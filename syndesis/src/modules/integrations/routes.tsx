/* tslint:disable:object-literal-sort-keys */
import { include } from 'named-urls';

export default {
  integrations: include('/integrations', {
    list: '',
    create: include('create', {
      begin: '',
      start: include(':integrationData/start', {
        configureAction: `:connectionId/:actionId/:step?`,
        selectAction: `:connectionId`,
      }),
      end: include(':integrationData/end', {
        configureAction: `:connectionId/:actionId/:step?`,
        selectAction: `:connectionId`,
      }),
    }),
  }),
};
