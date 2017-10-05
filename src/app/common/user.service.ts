import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';

import { User } from '../model';

@Injectable()
export class UserService {
  private _user = new BehaviorSubject(<User>{});

  get user() {
    return this._user.asObservable();
  }

  setUser(u: User) {
    this._user.next(u);
  }
}
