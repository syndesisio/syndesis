// Integration states
export const PUBLISHED = 'Published';
export const UNPUBLISHED = 'Unpublished';
export const PENDING = 'Pending';
export const ERROR = 'Error';

export type IntegrationState =
  | 'Published'
  | 'Unpublished'
  | 'Pending'
  | 'Error';

// Detailed state log link types
export const LOGS = 'LOGS';
export const EVENTS = 'EVENTS';

export type LinkType = 'LOGS' | 'EVENTS';

export interface IIntegrationDetailHistoryItem {
  updatedAt: string;
  version: number;
}

export interface IIntegrationDetailHistory {
  draft: boolean;
  items: IIntegrationDetailHistoryItem[];
}
