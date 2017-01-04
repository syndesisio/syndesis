import { BaseEntity, Entities } from '../entity/entity.model';

export interface Connection extends BaseEntity {
  configuredProperties: any;
  createdBy: string;
  createdOn: Date;
  description: string;
  icon: string;
  modifiedBy: string;
  modifiedOn: Date;
  name: string;
  position: string;
  type: string;
}

export type Connections = Array<Connection>;

export type ConnectionEntities = Entities<Connection>;
