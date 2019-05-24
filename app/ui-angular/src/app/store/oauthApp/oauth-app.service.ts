import { Injectable } from '@angular/core';

import { ApiHttpService } from '@syndesis/ui/platform';
import { OAuthApp, OAuthApps } from '@syndesis/ui/settings';
import { RESTService } from '@syndesis/ui/store/entity';
import { ConfigService } from '@syndesis/ui/config.service';

@Injectable()
export class OAuthAppService extends RESTService<OAuthApp, OAuthApps> {
  constructor(apiHttpService: ApiHttpService, configService: ConfigService) {
    super(apiHttpService, 'setup/oauth-apps', 'oauth-app', configService);
  }
}
