export interface ICiCdListPageItem {
  name: string;
  i18nUsesText: string;
}

export interface ITagIntegrationEntry {
  name: string;
  selected: boolean;
}

export enum TagNameValidationError {
  NoErrors = 'NoErrors',
  NoName = 'NoName',
  NameInUse = 'NameInUse',
}
