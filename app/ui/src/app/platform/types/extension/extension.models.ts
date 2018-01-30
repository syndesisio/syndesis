import { Action, BaseEntity } from '@syndesis/ui/platform';

export interface Extension extends BaseEntity {
  description: string;
  icon: string;
  extensionId: string;
  version: string;
  tags: Array<string>;
  actions: Array<Action>;
  dependencies: Array<string>;
  status: 'Draft' | 'Installed' | 'Deleted';
  schemaVersion: string;
  properties: {};
  configuredProperties: {};
  extensionType: string;
}
export type Extensions = Array<Extension>;
