import { Injectable, Input } from '@angular/core';
import { Restangular } from 'ngx-restangular';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { Http } from '@angular/http';
import { Observable } from 'rxjs/Rx';

import { ConfigService } from '../config.service';
import { User } from '../model';

@Injectable()
export class UserService {
  private _user: BehaviorSubject<User>;
  private _restangularService: Restangular;
  private apiBaseUrl = ConfigService['apiBase'] + ConfigService['apiEndpoint'];

  /**
   * UserService constructor
   * @param {Http} http
   * @param {Restangular} restangular
   */
  constructor(private http: Http, restangular: Restangular) {
    this._restangularService = restangular.service('users');
  }

  get user() {
    if (!this._user) {
      this._user = new BehaviorSubject(<User>{});
      this.initializeCurrentUser();
    }
    return this._user.asObservable();
  }

  /**
   * Set state of Guided Tour
   */
  setTourState(val): void {
    return localStorage.setItem('guidedTourState', JSON.stringify(val));
  }

  /**
   * Get state of Guided Tour
   */
  getTourState() {
    return JSON.parse(localStorage.getItem('guidedTourState'));
  }

  /**
   * Log the user out
   */
  logout(): Observable<any> {
    return this.http.get(this.apiBaseUrl + '/oauth/sign_out').map((res: any) => res.json());
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
