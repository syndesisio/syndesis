import { makeResolverNoParams } from '@syndesis/utils';
import routes from './routes';

export default {
  root: makeResolverNoParams(routes.root),
};
