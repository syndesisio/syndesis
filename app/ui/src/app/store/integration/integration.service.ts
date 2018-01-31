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

  public deploy(integration: Integration): Observable<Response> {
    const url = this.restangularService.one(integration.id).one('deployments').getRestangularUrl();
    return this.http.put(url, {});
  }

  public undeploy(integration: Integration): Observable<Response> {
    const url = this.restangularService.one(integration.id).one('deployments').one(integration.deploymentVersion).one('targetState');
    return this.http.post(url, {'targetState': 'Undeployed'});
  }

}
