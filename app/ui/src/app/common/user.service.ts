import { Injectable, Input } from '@angular/core';
import { Restangular } from 'ngx-restangular';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { Http } from '@angular/http';
import { Observable } from 'rxjs/Rx';

import { ConfigService } from '../config.service';
import { User } from '../model';

@Injectable()
export class UserService {
  private userSubject: BehaviorSubject<User>;
  private restangularService: Restangular;
  private apiBaseUrl = ConfigService['apiBase'] + ConfigService['apiEndpoint'];

  /**
   * UserService constructor
   * @param {Http} http
   * @param {Restangular} restangular
   */
  constructor(private http: Http, restangular: Restangular) {
    this.restangularService = restangular.service('users');
  }

  get user() {
    if (!this.userSubject) {
      this.userSubject = new BehaviorSubject(<User>{});
      this.initializeCurrentUser();
    }
    return this.userSubject.asObservable();
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
    return this.http
      .get(this.apiBaseUrl + '/oauth/sign_out')
      .map((res: any) => res.json());
  }

  setUser(user: User) {
    this.userSubject.next(user);
  }

  private initializeCurrentUser() {
    // @FIXME: `first()` is perhaps unnecessary here. Angular's Http (and HttpClient)
    //  Apis flag the observable stream as COMPLETE (and hence finish the subscription stream)
    // when the response is received, hence leaving the `.first()` RxJS call as unnecessary
    this.restangularService
      .one('~')
      .get()
      .first()
      .subscribe(user => {
        this.setUser(user);
      });
  }
}
