import {BaseEntity} from '../../model';

export interface ApiConnector extends BaseEntity {
  name: string;
}
export type ApiConnectors = Array<ApiConnector>;

