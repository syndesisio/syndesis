import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';

import { User } from './user.model';

@Injectable()
export class UserService {

  private _user = new BehaviorSubject({});

  get user() { return this._user.asObservable(); }

  setUser(u: User) {
    this._user.next(u);
  }

}
