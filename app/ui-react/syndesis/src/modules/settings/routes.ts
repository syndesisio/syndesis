import { include } from 'named-urls';

export default include('/settings', {
  oauthApps: include('oauth-apps', {
    root: '',
  }),
  root: '',
});
