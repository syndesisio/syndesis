import { BaseResource } from './baseresource.model';

export interface Integration extends BaseResource {
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
