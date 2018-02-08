import { BaseReducerModel } from '@syndesis/ui/platform';

/**
 * Step 1: Integration Upload
 */

export interface IntegrationImportUploadState extends BaseReducerModel {
  file?: File;
  importResults: {
    integrations?: string[];
    connections?: string[];
  };
  list: IntegrationImportsUploadState;

}

export type IntegrationImportsUploadState = Array<IntegrationImportUploadState>;


/**
 * Step 2: Integration Creation
 * Submitting the uploaded item to be created.
 */

export interface IntegrationImportEditState extends BaseReducerModel {
  file?: File;
  importResults: {
    integrations?: string[];
    connections?: string[];
  };
  list: IntegrationImportsEditState;
}

export type IntegrationImportsEditState = Array<IntegrationImportEditState>;


