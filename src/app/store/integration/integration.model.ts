import { Moment } from 'moment';

import { BaseEntity } from '../entity/entity.model';

export interface Step extends BaseEntity {
  configuredProperties: string;
}

export interface Integration extends BaseEntity {
  configuredProperties: Map<string, any>;
  createdBy: string;
  createdOn: Moment;
  description: string;
  icon: string;
  modifiedBy: string;
  modifiedOn: Moment;
  name: string;
  steps: Array<Step>;
}

export type Integrations = Array<Integration>;
