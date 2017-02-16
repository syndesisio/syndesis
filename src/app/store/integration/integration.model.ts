import { Moment } from 'moment';

import { BaseEntity } from '../entity/entity.model';
import { Connection } from '../connection/connection.model';
export interface Step extends BaseEntity {
  configuredProperties: string;
}

export interface Integration extends BaseEntity {
  configuration: string;
  createdBy: string;
  createdOn: Moment;
  description: string;
  icon: string;
  modifiedBy: string;
  modifiedOn: Moment;
  name: string;
  steps: Array<Step>;
  connections: Array<Connection>;
}

export type Integrations = Array<Integration>;
