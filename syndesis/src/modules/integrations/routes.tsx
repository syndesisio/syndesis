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
      end: include('end/:integrationData', {
        configureAction: `:connectionId/:actionId/:step?`,
        selectAction: `:connectionId`,
        selectConnection: ``,
      }),
    }),
  }),
};
