import { Step } from '@syndesis/ui/platform';

export class FlowEvent {
  kind: string;
  [name: string]: any;
}

export enum FlowErrorKind {
  NO_START_CONNECTION,
  NO_FINISH_CONNECTION,
  NO_NAME,
}

export class FlowError {
  kind: FlowErrorKind;
  [name: string]: any;
}

export interface IndexedStep {
  step: Step;
  index: number;
}

// Mucking with the integration actions
export const INTEGRATION_UPDATED = 'integration-updated';
export const INTEGRATION_INSERT_STEP = 'integration-insert-step';
export const INTEGRATION_INSERT_DATAMAPPER = 'integration-insert-datamapper';
export const INTEGRATION_INSERT_CONNECTION = 'integration-insert-connection';
export const INTEGRATION_REMOVE_STEP = 'integration-remove-step';
export const INTEGRATION_SET_STEP = 'integration-set-step';
export const INTEGRATION_SET_METADATA = 'integration-set-metadata';
export const INTEGRATION_SET_PROPERTIES = 'integration-set-properties';
export const INTEGRATION_SET_DESCRIPTOR = 'integration-set-descriptor';
export const INTEGRATION_SET_ACTION = 'integration-set-action';
export const INTEGRATION_SET_DATASHAPE = 'integration-set-datashape';
export const INTEGRATION_SET_CONNECTION = 'integration-set-connection';
export const INTEGRATION_SET_PROPERTY = 'integration-set-property';
export const INTEGRATION_SAVE = 'integration-save';
export const INTEGRATION_SAVED = 'integration-saved';
export const INTEGRATION_ADD_FLOW = 'integration-add-flow';
export const INTEGRATION_REMOVE_FLOW = 'integration-remove-flow';

// UI actions
export const INTEGRATION_CANCEL_CLICKED = 'integration-cancel-clicked';
export const INTEGRATION_DONE_CLICKED = 'integration-done-clicked';
export const INTEGRATION_DELETE_PROMPT = 'integration-delete-prompt';
export const INTEGRATION_SIDEBAR_EXPAND = 'integration-sidebar-expand';
export const INTEGRATION_SIDEBAR_COLLAPSE = 'integration-sidebar-collapse';
export const INTEGRATION_BUTTON_DISABLE_DONE =
  'integration-button-disable-done';
export const INTEGRATION_BUTTON_ENABLE_DONE = 'integration-button-enable-done';
