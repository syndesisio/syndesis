import { Injectable } from '@angular/core';

import { Restangular } from 'ngx-restangular';

import { RESTService } from '../entity/rest.service';
import { Integration, Integrations, IntegrationState } from '@syndesis/ui/platform';
import { Observable } from 'rxjs/Observable';
import { Http, Response, ResponseContentType } from '@angular/http';

@Injectable()
export class IntegrationService extends RESTService<Integration, Integrations> {
  constructor(restangular: Restangular, private http: Http) {
    super(restangular.service('integrations'), 'integration');
  }

}
