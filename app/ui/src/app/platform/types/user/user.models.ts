import { BaseEntity } from '@syndesis/ui/platform';

export interface User extends BaseEntity {
  fullName: string;
  name: string;
  organizationId: string;
  username: string;
  firstName: string;
  lastName: string;
  // TODO
  //integrations: Array<Integration>;
  roleId: string;
  id: string;
}

export type Users = Array<User>;
