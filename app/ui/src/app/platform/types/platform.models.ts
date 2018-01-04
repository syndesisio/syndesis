/**
 * StringMap allows to model unboundered hash objects
 */
export interface StringMap<T> {
  [key: string]: T;
}

/**
 * FileMap allows to model FormData objects containing files mapped to named keys
 */
export interface FileMap {
  [key: string]: File;
}

/**
 * A convenience model to map internal UI errors, either derived from Http sync operations
 * or any other state handling actions that might throw an exception.
 */
export interface ActionReducerError {
  errorCode?: any;
  message: string;
  debugMessage?: string;
  status?: number;
  statusText?: string;
}

/**
 * BaseReducerModel must be applied to all interfaces modelling
 * a NgRX-managed slice of state featuring its own reducer.
 */
export interface BaseReducerModel {
  loading?: boolean;
  loaded?: boolean;
  hasErrors?: boolean;
  errors?: Array<ActionReducerError>;
}

/**
 * Common interface for modelling requests requiring several steps for accomplishment
 * which require additional flags to track progress level and success
 */
export interface BaseRequestModel {
  isRequested?: boolean;
  isOK?: boolean;
  isComplete?: boolean;
}
