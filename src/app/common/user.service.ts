import { Injectable } from '@angular/core';
import { Restangular } from 'ngx-restangular';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';

import { User } from '../model';

@Injectable()
export class UserService {
  private _user: BehaviorSubject<User>;
  private _restangularService: Restangular;

  constructor(restangular: Restangular) {
    this._restangularService = restangular.service('users');
  }

  get user() {
    if (!this._user) {
      this._user = new BehaviorSubject(<User>{});
      this.initializeCurrentUser();
    }
    return this._user.asObservable();
  }

  setUser(u: User) {
    this._user.next(u);
  }

  private initializeCurrentUser() {
    this._restangularService.one('~').get().first().subscribe((user) => {
      this.setUser(user);
    });
  }
}
