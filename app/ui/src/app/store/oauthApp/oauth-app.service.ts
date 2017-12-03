import { Injectable } from '@angular/core';

import { Restangular } from 'ngx-restangular';

import { RESTService } from '../entity/rest.service';
import { OAuthApp, OAuthApps } from '../../model';

@Injectable()
export class OAuthAppService extends RESTService<OAuthApp, OAuthApps> {
  constructor(restangular: Restangular) {
    super(restangular.service('setup/oauth-apps'), 'oauth-app');
  }
}
