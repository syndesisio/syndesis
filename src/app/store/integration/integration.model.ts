import { Moment } from 'moment';

import { BaseEntity } from '../entity/entity.model';

export interface Integration extends BaseEntity {
  configuredProperties: Map<string, string>;
  createdBy: string;
  createdOn: Moment;
  description: string;
  icon: string;
  modifiedBy: string;
  modifiedOn: Moment;
  name: string;
  position: string;
  type: string;
}

export type Integrations = Array<Integration>;
