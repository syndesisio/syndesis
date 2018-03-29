import { Injectable } from '@angular/core';

import { ApiHttpService } from '@syndesis/ui/platform';
import { OAuthApp, OAuthApps } from '@syndesis/ui/settings';
import { RESTService } from '../entity';

@Injectable()
export class OAuthAppService extends RESTService<OAuthApp, OAuthApps> {
  constructor(apiHttpService: ApiHttpService) {
    super(apiHttpService, 'setup/oauth-apps', 'oauth-app');
  }
}
