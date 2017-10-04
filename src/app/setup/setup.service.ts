import { Injectable } from '@angular/core';
import { Http, RequestOptionsArgs, Headers, Response } from '@angular/http';
import { OAuthService } from 'angular-oauth2-oidc-hybrid';
import { ConfigService } from '../config.service';
import { Setup } from '../model';
import { log } from '../logging';

@Injectable()
export class SetupService {

  private url: string;
  private args: RequestOptionsArgs;

  /**
   * @type {boolean}
   * Flag used to determine whether or not the user is a first time user.
   */
  firstTime = true;

  constructor(private http: Http) {
  }

  setApiEndpoint(apiEndpoint: string) {
    this.url = apiEndpoint.substring(0, apiEndpoint.lastIndexOf('/')) + '/setup';
  }

  setAccessToken(accessToken: string) {
    this.args = {
      headers: new Headers({
        'Authorization': `Bearer ${accessToken}`,
      }),
    };
  }

  /**
   * Function that checks if GitHub account setup is required or has already been completed.
   * Simultaneously sets the firstTime flag.
   * 204 status from API = GitHub setup required
   * 410 status from API = GitHub setup NOT required
   * @returns {Promise<boolean>}
   */
  isSetupPending(): Promise<boolean> {
    return this.http.get(this.url, this.args)
      .toPromise()
      .then(response => {
        if (response.status === 204) {
          this.firstTime = true;
          return true;
        } else {
          return this.handleError('Failed to check GitHub credentials', response);
        }
      })
      .catch((response: Response) => {
        if (response.status === 410) {
          this.firstTime = false;
          return false;
        } else {
          return this.handleError('Failed to check GitHub credentials', response);
        }
      });
  }

  /**
   * Step 2 - Save GitHub credentials or update the setup.
   * @param {Setup} setup
   * @returns {Promise<any>}
   */
  updateSetup(setup: Setup): Promise<any> {
    return this.http.put(this.url, JSON.stringify(setup), this.args)
      .toPromise()
      .then(response => null)
      .catch((response: Response) => {
        // 410 (Gone) if already configured
        return response.status === 410 ? null : this.handleError('Failed to save GitHub credentials', response);
      });
  }

  /**
   * Handles errors.
   * @param {string} message
   * @param {Response} response
   * @returns {Promise<any>}
   */
  private handleError(message: string, response: Response): Promise<any> {
    log.error(message, new Error(response.toString()));
    return Promise.reject(message);
  }

}
