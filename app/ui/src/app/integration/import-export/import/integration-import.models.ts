import { BaseReducerModel } from '@syndesis/ui/platform';

export interface IntegrationImportState extends BaseReducerModel {
  file?: File;
  importResults: {
    integrations?: string[];
    connections?: string[];
  };
  list: IntegrationImportsState;

}

export type IntegrationImportsState = Array<IntegrationImportState>;


