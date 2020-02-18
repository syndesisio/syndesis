export const NOTFOUND = 'NOTFOUND';
export const SUBMITTED = 'SUBMITTED';
export const COMPLETE = 'COMPLETE';
export const CONFIGURING = 'CONFIGURING';
export const BUILDING = 'BUILDING';
export const DEPLOYING = 'DEPLOYING';
export const RUNNING = 'RUNNING';
export const FAILED = 'FAILED';
export const CANCELLED = 'CANCELLED';
export const DELETE_SUBMITTED = 'DELETE_SUBMITTED';
export const DELETE_REQUEUE = 'DELETE_REQUEUE';
export const DELETE_DONE = 'DELETE_DONE';
export const UNPUBLISH_SUBMITTED = 'UNPUBLISH_SUBMITTED';

export type VirtualizationPublishState =
  | 'NOTFOUND'
  | 'SUBMITTED'
  | 'COMPLETE'
  | 'CONFIGURING'
  | 'BUILDING'
  | 'DEPLOYING'
  | 'RUNNING'
  | 'FAILED'
  | 'CANCELLED'
  | 'DELETE_SUBMITTED'
  | 'DELETE_REQUEUE'
  | 'DELETE_DONE'
  | 'UNPUBLISH_SUBMITTED';

// Detailed state log link types
export const LOGS = 'LOGS';
export const EVENTS = 'EVENTS';

export type LinkType = 'LOGS' | 'EVENTS';
