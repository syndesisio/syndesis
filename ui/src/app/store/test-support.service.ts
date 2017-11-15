import { Injectable } from '@angular/core';
import { Http } from '@angular/http';
import { Restangular } from 'ngx-restangular';

@Injectable()
export class TestSupportService {
  service: Restangular = undefined;

  constructor(public restangular: Restangular, public http: Http) {
    this.service = restangular.service('test-support');
  }

  resetDB() {
    const url = this.service.one('reset-db').getRestangularUrl();
    return this.http.get(url);
  }

  snapshotDB() {
    const url = this.service.one('snapshot-db').getRestangularUrl();
    return this.http.get(url);
  }

  restoreDB(data: any) {
    const url = this.service.one('restore-db').getRestangularUrl();
    return this.http.post(url, data);
  }
}
