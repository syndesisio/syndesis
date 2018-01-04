import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { log, getCategory } from './logging';
import { environment } from '../environments/environment';

const apiEndpoints = require('./../api-endpoints-config.json');

const category = getCategory('ConfigService');

const defaults = environment.config;

const defaultConfigJson = '/config.json';

@Injectable()
export class ConfigService {
  private settingsRepository = defaults;

  constructor(private httpClient: HttpClient) {
    this.settingsRepository = this.getSettings();
  }

  load(configJson = defaultConfigJson): Promise<any|ConfigService> {
    return this.httpClient.get(configJson)
      .toPromise()
      .then(config => {
        log.debugc(() => 'Received config: ' + JSON.stringify(config, undefined, 2), category);

        const apiChildEndpoints = JSON.parse(apiEndpoints);
        this.settingsRepository = Object.freeze({
          ...this.settingsRepository,
          ...config,
          ...apiChildEndpoints
        });

        log.debugc(() =>
          'Using merged config: ' + JSON.stringify(this.settingsRepository, undefined, 2),
          category
        );

        return this;
      })
      .catch(() => {
        log.warnc(() =>
          'Error: Configuration service unreachable! Using defaults: ' +
          JSON.stringify(this.settingsRepository),
          category
        );

        Promise.resolve();
      });
  }

  getSettings(group?: string, key?: string, def?: any): any {
    if (!group) {
      return this.settingsRepository;
    }

    if (!this.settingsRepository[group]) {
      if (def) {
        return def;
      }
      throw new Error(
        `Error: No setting found with the specified group [${group}]!`
      );
    }

    if (!key) {
      return this.settingsRepository[group];
    }

    if (this.settingsRepository[group][key] === undefined) {
      if (def) {
        return def;
      }
      throw new Error(
        `Error: No setting found with the specified group/key [${group}/${key}]!`
      );
    }

    return this.settingsRepository[group][key];
  }
}

export function appConfigInitializer(configService: ConfigService): () => Promise<ConfigService> {
  return () => configService.load();
}
