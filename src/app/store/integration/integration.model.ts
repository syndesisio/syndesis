import { BaseEntity, Entities } from '../entity/entity.model';

export interface Integration extends BaseEntity {
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

export type Integrations = Array<Integration>;

export type IntegrationEntities = Entities<Integration>;
