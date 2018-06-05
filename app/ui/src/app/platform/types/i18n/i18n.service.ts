import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { I18NState } from './i18n.models';

@Injectable()
export abstract class I18NService {
  abstract setLocale(locale: string): Observable<I18NState>;

  abstract localize(dictionaryKey: string, args?: any[]): string;

  abstract persistLocale(locale: string): void;

  abstract getLocale(defaultLocale: string): string;

  abstract getValue(dictionaryKey: string, args?: any[]): Observable<string>;
}
