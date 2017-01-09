import {Moment} from 'moment';

import {BaseEntity} from '../entity/entity.model';

export interface Template extends BaseEntity {
  configuredProperties: any;
  createdBy: string;
  createdOn: Moment;
  description: string;
  icon: string;
  modifiedBy: string;
  modifiedOn: Moment;
  name: string;
  type: string;
}

export type Templates = Array<Template>;
