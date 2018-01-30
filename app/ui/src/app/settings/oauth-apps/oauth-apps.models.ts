import { BaseEntity } from '@syndesis/ui/platform';

export interface OAuthApp extends BaseEntity {
  icon: string;
  clientId: string;
  clientSecret: string;
}

export type OAuthApps = Array<OAuthApp>;
