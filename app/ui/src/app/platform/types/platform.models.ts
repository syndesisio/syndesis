/**
 * StringMap allows to model unboundered hash objects
 */
export interface StringMap<T> {
  [key: string]: T;
}

/**
 * A convenience model to map internal UI errors, either derived from Http sync operations
 * or any other state handling actions that might throw an exception.
 */
export interface ActionReducerError {
  errorCode: any;
  message: string;
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
 * Common wrapper interface for all messages exchanged between UI and API
 */
export interface RestResponse<T> {
  kind: string;
  data: T;
}

/**
 * Maps errors returned from the REST API. Currently unused until we figure out a
 * common contract schema for ALL messages exchanged between the UI and the API.
 */
export interface RestError {
  developerMsg: string;
  userMsg: string;
  userMsgDetail: string;
  errorCode: number;
}
