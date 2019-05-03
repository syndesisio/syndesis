/* tslint:disable:object-literal-sort-keys no-empty-interface */
import { Extension } from '@syndesis/models';
import { makeResolver, makeResolverNoParams } from '@syndesis/utils';
import routes from './routes';

export default {
  extension: {
    details: makeResolver<{ extension: Extension }>(
      routes.extension.details,
      ({ extension }) => ({
        params: {
          extensionId: extension.id,
        },
        state: {
          extension,
        },
      })
    ),
    update: makeResolver<{ extension: Extension }>(
      routes.extension.update,
      ({ extension }) => ({
        params: {
          extensionId: extension.id,
        },
        state: {
          extension,
        },
      })
    ),
  },
  import: makeResolverNoParams(routes.import),
  list: makeResolverNoParams(routes.list),
};
