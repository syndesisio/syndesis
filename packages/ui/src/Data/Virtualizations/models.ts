export const NOTFOUND = 'NOTFOUND';
export const SUBMITTED = 'SUBMITTED';
export const CONFIGURING = 'CONFIGURING';
export const BUILDING = 'BUILDING';
export const DEPLOYING = 'DEPLOYING';
export const RUNNING = 'RUNNING';
export const FAILED = 'FAILED';
export const CANCELLED = 'CANCELLED';

export type VirtualizationPublishState =
  | 'NOTFOUND'
  | 'SUBMITTED'
  | 'CONFIGURING'
  | 'BUILDING'
  | 'DEPLOYING'
  | 'RUNNING'
  | 'FAILED'
  | 'CANCELLED';

// Detailed state log link types
export const LOGS = 'LOGS';
export const EVENTS = 'EVENTS';

export type LinkType = 'LOGS' | 'EVENTS';
