import { BaseReducerModel } from '@syndesis/ui/platform';

/**
 * POC: The MetadataState provides a wrapping model for application-wide
 * state values and serves as an example of implementation of a global slice
 * of the overall application state manage by the central NGRX store.
 */
export interface MetadataState extends BaseReducerModel {
  appName?: string;
  locale?: string;
}
