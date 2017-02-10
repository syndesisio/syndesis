import { Injectable } from '@angular/core';
import { Http } from '@angular/http';

import * as _ from 'lodash';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';

import { log, getCategory } from './logging';

const category = getCategory('ConfigService');

const defaults = Object.freeze({
  apiEndpoint: 'http://localhost:8080/v1',
  title: 'Red Hat iPaaS',
  oauth: {
    authorize: 'https://api.rh-idev.openshift.com/oauth/authorize',
    userInfo: 'https://api.rh-idev.openshift.com/oapi/v1/users/~',
    clientId: 'system:serviceaccount:staging:ipaas-client',
    scopes: ['user:info', 'user:check-access', 'role:edit:staging:!'],
  },
});

const defaultConfigJson = '/config.json';

export function configServiceInitializer(config: ConfigService) {
  return () => config.load();
}

@Injectable()
export class ConfigService {

  private settingsRepository: any = defaults;

  constructor(private _http: Http) { }

  load(configJson: string = defaultConfigJson): Promise<ConfigService> {
    return this._http.get(configJson).map(res => res.json())
      .toPromise()
      .then((config) => {
        log.infoc(() => 'Received config: ' + JSON.stringify(config, undefined, 2), category);
        this.settingsRepository = Object.freeze(_.merge({}, this.settingsRepository, config));
        return this;
      })
      .catch(() => {
        log.warnc(() => 'Error: Configuration service unreachable!', category);
      });
  }

  getSettings(group?: string, key?: string): any {
    if (!group) {
      return this.settingsRepository;
    }

    if (!this.settingsRepository[group]) {
      throw new Error(`Error: No setting found with the specified group [${group}]!`);
    }

    if (!key) {
      return this.settingsRepository[group];
    }

    if (!this.settingsRepository[group][key]) {
      throw new Error(`Error: No setting found with the specified group/key [${group}/${key}]!`);
    }

    return this.settingsRepository[group][key];
  }

}
