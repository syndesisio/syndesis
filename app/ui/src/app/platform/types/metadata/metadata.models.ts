import { BaseReducerModel } from '@syndesis/ui/platform';

export interface MetadataState extends BaseReducerModel {
  appName?: string;
  locale?: string;
}

export interface MetadataStore {
  metadataState: MetadataState;
}
