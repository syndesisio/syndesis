import { BaseEntity, StringMap, ConfigurationProperty } from '@syndesis/ui/platform';

export interface OAuthApp extends BaseEntity {
  icon: string;
  configuredProperties: StringMap<string>;
  properties: StringMap<ConfigurationProperty>;
}

export type OAuthApps = Array<OAuthApp>;
