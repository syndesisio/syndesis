import { BaseEntity } from '@syndesis/ui/platform';

export interface User extends BaseEntity {
  fullName?: string;
  name?: string;
  organizationId?: string;
  username?: string;
  firstName?: string;
  lastName?: string;
  roleId?: string;
}

export type Users = Array<User>;
