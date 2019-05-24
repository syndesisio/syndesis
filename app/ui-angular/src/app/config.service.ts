import { Subject, Observable } from 'rxjs';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { log, getCategory } from '@syndesis/ui/logging';
import { environment } from 'environments/environment';

const category = getCategory('ConfigService');

const defaults = environment.config;

const defaultConfigJson = '/config.json';

@Injectable()
export class ConfigService {
  asyncSettings$: Observable<any>;
  private settingsRepository = defaults;
  private settingsSubject = new Subject<any>();

  constructor(private httpClient: HttpClient) {
    this.asyncSettings$ = this.settingsSubject.asObservable();

    this.settingsRepository = this.getSettings();
    this.settingsSubject.next(this.settingsRepository);
  }

  initialize(configJson = defaultConfigJson): Promise<any | ConfigService> {
    return this.httpClient
      .get(configJson)
      .toPromise()
      .then(config => {
        log.debug(
          () => 'Received config: ' + JSON.stringify(config, undefined, 2),
          category
        );

        this.settingsRepository = Object.freeze({
          ...this.settingsRepository,
          ...config
        });

        this.settingsSubject.next(this.settingsRepository);

        log.debug(
          () =>
            'Using merged config: ' +
            JSON.stringify(this.settingsRepository, undefined, 2),
          category
        );

        return this;
      })
      .catch(() => {
        log.warn(
          () =>
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

    if (this.settingsRepository[group] === undefined) {
      if (def != undefined) {
        return def;
      }
      throw new Error(
        `Error: No setting found with the specified group/key [${group}]!`
      );
    }

    if (!key) {
      return this.settingsRepository[group];
    }

    if (this.settingsRepository[group][key] === undefined) {
      if (def != undefined) {
        return def;
      }
      throw new Error(
        `Error: No setting found with the specified group/key [${group}/${key}]!`
      );
    }

    return this.settingsRepository[group][key];
  }
}

export function appConfigInitializer(
  configService: ConfigService
): () => Promise<ConfigService> {
  return () => configService.initialize();
}
