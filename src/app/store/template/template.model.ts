import { BaseEntity, Entities } from '../entity/entity.model';

export interface Template extends BaseEntity {
  configuredProperties: any;
  createdBy: string;
  createdOn: Date;
  description: string;
  icon: string;
  modifiedBy: string;
  modifiedOn: Date;
  name: string;
  type: string;
}

export type Templates = Array<Template>;

export type TemplateEntities = Entities<Template>;
