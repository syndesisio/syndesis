import { BaseReducerModel } from '@syndesis/ui/platform';

export interface Language {
  isoCode: string;
}

export interface DictionaryEntry {
  [context: string]: string | DictionaryEntry;
}

export interface I18NState extends BaseReducerModel {
  dictionary: DictionaryEntry;
  locale: string;
  onSync?: boolean;
  onError?: boolean;
}

export const I18N_DEFAULT_LOCALE = 'en-GB'; // TODO: Move this to ConfigService (or something better, please)
