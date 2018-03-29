import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs/Observable';

import {
  PlatformState,
  I18NState, selectI18NState,
  DictionaryEntry,
  I18NService,
} from '../../platform';

const EMPTY_STRING = '';
const I18N_DEFAULT_PLACEHOLDER = '?';
const STORAGE_LANGUAGE_KEY = 'syndesis-i18n-locale';
const DICTIONARY_PATH = '/assets/dictionary';

@Injectable()
export class I18NProviderService extends I18NService {
  private dictionary: DictionaryEntry;

  constructor(
    private httpClient: HttpClient,
    private platformStore: Store<PlatformState>,
    @Inject(PLATFORM_ID) private platformId: any,
    @Inject('LOCALSTORAGE') private localStorage: Storage
  ) {
    super();

    this.platformStore
      .select(selectI18NState)
      .map(state => state.dictionary)
      .subscribe(dictionary => this.dictionary = dictionary);
  }

  setLocale(locale: string): Observable<I18NState> {
    const dictionaryUrl = `${DICTIONARY_PATH}/${locale}.json`;
    return this.httpClient
      .get(dictionaryUrl)
      .catch(error => Observable.throw(error));
  }

  localize(dictionaryKey: string, args?: any[]): string {
    if (!this.dictionary) {
      return I18N_DEFAULT_PLACEHOLDER;
    }

    let translateKeys = (dictionaryKey || EMPTY_STRING).toLowerCase().split(/[\.:]/);

    if (translateKeys.length === 1) {
      translateKeys = ['shared', ...translateKeys];
    }

    let translation = this.getTranslatedTerm(this.dictionary, ...translateKeys);

    if (translation !== I18N_DEFAULT_PLACEHOLDER)  {
      translation = this.replaceLabelPlaceholders(translation, args);
      translation = this.replaceIndexPlaceholders(translation, args);
    }

    return translation;
  }

  persistLocale(locale: string): void {
    if (isPlatformBrowser(this.platformId)) {
      this.localStorage.setItem(STORAGE_LANGUAGE_KEY, locale);
    }
  }

  getLocale(defaultLocale: string): string {
    if (isPlatformBrowser(this.platformId)) {
      defaultLocale = this.localStorage.getItem(STORAGE_LANGUAGE_KEY) || defaultLocale;
    }

    return defaultLocale;
  }

  private getTranslatedTerm(dictionary: DictionaryEntry, ...keys: string[]): string {
    let translationMatch: DictionaryEntry | string = dictionary;
    let lastValidKey = keys[0];
    for (let index = 0; index < keys.length; index++) {
      const key = keys[index];
      translationMatch = translationMatch[key];
      if (!translationMatch) {
        return I18N_DEFAULT_PLACEHOLDER;
      }
      lastValidKey = key;
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
