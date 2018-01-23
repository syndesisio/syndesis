import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';

import { User } from '@syndesis/ui/model';

@Injectable()
export abstract class UserService {
  /**
   * Gets active user as an observable entity
   */
  abstract user: Observable<User>;

  /**
   * Set state of Guided Tour
   * @param val {any} TBD
   */
  abstract setTourState(val: any): void;

  /**
   * Get state of Guided Tour
   */
  abstract getTourState(): any;

  /**
   * Log the user out
   */
  abstract logout(): Observable<any>;
}
