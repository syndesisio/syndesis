import * as moment from 'moment';

import { BaseEntity } from '../entity/entity.model';

export interface Connection extends BaseEntity {
  configuredProperties: Map<string, string>;
  createdBy: string;
  createdOn: moment.Moment;
  description: string;
  icon: string;
  modifiedBy: string;
  modifiedOn: moment.Moment;
  name: string;
  type: string;
}


export type Connections = Array<Connection>;
