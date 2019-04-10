import { map, catchError } from 'rxjs/operators';
import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Store, select } from '@ngrx/store';
import { Observable, throwError } from 'rxjs';

import {
  PlatformState,
  I18NState,
  selectI18NState,
  DictionaryEntry,
  I18NService
} from '@syndesis/ui/platform';
import { environment } from 'environments/environment';
import { ConfigService } from '@syndesis/ui/config.service';

const {
  fallbackValue,
  localStorageKey,
  dictionaryFolderPath
} = environment.i18n;

@Injectable()
export class I18NProviderService extends I18NService {
  private dictionary: DictionaryEntry;

  constructor(
    private httpClient: HttpClient,
    private platformStore: Store<PlatformState>,
    private configService: ConfigService,
    @Inject(PLATFORM_ID) private platformId: any,
    @Inject('LOCALSTORAGE') private localStorage: Storage
  ) {
    super();

    this.platformStore
      .pipe(
        select(selectI18NState),
        map(state => state.dictionary)
      )
      .subscribe(dictionary => (this.dictionary = dictionary));
  }

  setLocale(locale: string): Observable<I18NState> {
    const dictionaryUrl = `${dictionaryFolderPath}/${locale}.json`;
    return this.httpClient
      .get<I18NState>(dictionaryUrl)
      .pipe(catchError(error => throwError(error)));
  }

  localize(dictionaryKey: string, args?: any[]): string {
    if (!this.dictionary) {
      return fallbackValue;
    }
    // config.json overrides
    switch (dictionaryKey) {
      case 'shared.consoleurl':
      case 'consoleurl':
        return this.configService.getSettings('consoleUrl');
      case 'shared.project.name':
      case 'project.name':
        return this.configService.getSettings('branding', 'appName');
      default:
    }
    let translateKeys = (dictionaryKey || '').toLowerCase().split(/[\.:]/);
    if (translateKeys.length === 1) {
      translateKeys = ['shared', ...translateKeys];
    }
    let translation = this.getTranslatedTerm(this.dictionary, ...translateKeys);
    if (translation !== fallbackValue) {
      translation = this.replaceLabelPlaceholders(translation, args);
      translation = this.replaceIndexPlaceholders(translation, args);
    }
    return translation;
  }

  persistLocale(locale: string): void {
    if (isPlatformBrowser(this.platformId)) {
      this.localStorage.setItem(localStorageKey, locale);
    }
  }

  getLocale(defaultLocale: string): string {
    if (isPlatformBrowser(this.platformId)) {
      defaultLocale =
        this.localStorage.getItem(localStorageKey) || defaultLocale;
    }

    return defaultLocale;
  }

  getValue(dictionaryKey: string, args?: any[]): Observable<string> {
    return this.platformStore
      .pipe(
        select(selectI18NState),
        map(() => this.localize(dictionaryKey, args))
      );
  }

  private getTranslatedTerm(
    dictionary: DictionaryEntry,
    ...keys: string[]
  ): string {
    let translationMatch: DictionaryEntry | string = dictionary;
    for (let index = 0; index < keys.length; index++) {
      const key = keys[index];
      translationMatch = translationMatch[key];

      if (!translationMatch) {
        return fallbackValue;
      }
    }

    return translationMatch.toString();
  }

  private replaceLabelPlaceholders(value: string, args: any[]): string {
    return value.replace(/\{\{(\D*?)\}\}/g, (fullMatch, ...matchGroups) => {
      return this.localize(matchGroups[0].trim(), args);
    });
  }

  private replaceIndexPlaceholders(value: string, args: any[]): string {
    if (Array.isArray(args) && args.length > 0) {
      return value.replace(/\{\{(\d*?)\}\}/g, (fullMatch, ...matchGroups) => {
        const index = parseInt(matchGroups[0], 10);
        return args[index];
      });
    } else if (args && args.toString() !== '') {
      return value.replace(/\{\{(\d*?)\}\}/g, args.toString());
    }

    return value;
  }
}
