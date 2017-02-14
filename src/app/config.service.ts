import { Injectable } from '@angular/core';
import { Http } from '@angular/http';

import * as _ from 'lodash';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/toPromise';

import { log, getCategory } from './logging';
import { environment } from '../environments/environment';

const category = getCategory('ConfigService');

const defaults = environment.config;

const defaultConfigJson = '/config.json';

@Injectable()
export class ConfigService {

  private settingsRepository: any = defaults;

  constructor(private _http: Http) { }

  load(configJson: string = defaultConfigJson): Promise<ConfigService> {
    return this._http.get(configJson).map(res => res.json())
      .toPromise()
      .then((config) => {
        log.debugc(() => 'Received config: ' + JSON.stringify(config, undefined, 2), category);
        this.settingsRepository = Object.freeze(_.merge({}, this.settingsRepository, config));
        log.debugc(() => 'Using merged config: ' + JSON.stringify(this.settingsRepository, undefined, 2), category);
        return this;
      })
      .catch(() => {
        log.warnc(() => 'Error: Configuration service unreachable! Using defaults: ' + JSON.stringify(this.settingsRepository), category);
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

    if (this.settingsRepository[group][key] === undefined) {
      throw new Error(`Error: No setting found with the specified group/key [${group}/${key}]!`);
    }

    return this.settingsRepository[group][key];
  }

}
