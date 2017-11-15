import { Injectable } from '@angular/core';
import { Restangular } from 'ngx-restangular';
import { Subscription } from 'rxjs/Subscription';
import { Observable } from 'rxjs/Observable';

import { RESTService } from '../entity/rest.service';
import { Connector, Connectors } from '../../model';

interface AcquisitionResponseState {
  persist: string;
  spec: string;
}

interface AcquisitionResponse {
  redirectUrl: string;
  type: string;
  state: AcquisitionResponseState;
}

@Injectable()
export class ConnectorService extends RESTService<Connector, Connectors> {
  constructor(restangular: Restangular) {
    super(restangular.service('connectors'), 'connector');
  }

  validate(id: string, data: Map<string, string>) {
    return this.restangularService
      .one(id)
      .one('verifier')
      .customPOST(data);
  }

  credentials(id: string) {
    return this.restangularService
      .one(id)
      .one('credentials')
      .get();
  }

  acquireCredentials(id: string) {
    // need to save the state of the app a bit since this
    // will navigate to a new page and then come back
    return Observable.create(observer => {
      // TODO we probably don't need all these nested setTimeouts, but...
      setTimeout(() => {
        // Try and clear any stale cookies, though we can't touch HttpOnly ones
        document.cookie.split(';').forEach(function(c) {
          if (c.startsWith('cred-')) {
            const newCookie = c
              .replace(/^ +/, '')
              .replace(
                /=.*/,
                '=;expires=' + new Date().toUTCString() + ';path=/'
              );
          }
        });

        setTimeout(() => {
          this.restangularService
            .one(id)
            .post('credentials', {
              returnUrl: window.location.pathname + '#' + id
            })
            .subscribe((resp: AcquisitionResponse) => {
              document.cookie = resp.state.spec;
              setTimeout(() => {
                window.location.href = resp.redirectUrl;
                observer.next(resp);
                observer.complete();
              }, 30);
            });
        }, 30);
      }, 30);
    });
  }
}
