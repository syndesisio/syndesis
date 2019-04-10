import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { User } from '@syndesis/ui/platform/types/user/user.models';

@Injectable()
export abstract class UserService {
  /**
   * Gets active user as an observable entity
   */
  abstract user: Observable<User>;

  /**
   * Log the user out
   */
  abstract logout(): void;
}
