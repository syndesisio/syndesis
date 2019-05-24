import { makeResolverNoParams } from '@syndesis/utils';
import routes from './routes';

export default {
  oauthApps: makeResolverNoParams(routes.oauthApps.root),
  root: makeResolverNoParams(routes.root),
};
