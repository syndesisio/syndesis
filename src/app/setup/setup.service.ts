import { Injectable } from '@angular/core';
import { Http, RequestOptionsArgs, Headers, Response } from '@angular/http';
import { OAuthService } from 'angular-oauth2-oidc-hybrid';
import { ConfigService } from '../config.service';
import { Router } from '@angular/router';
import { Setup } from '../model';
import { log } from '../logging';
import {
  NotificationType,
  NotificationService,
} from 'patternfly-ng';

@Injectable()
export class SetupService {

  private url: string;
  private args: RequestOptionsArgs;

  /**
   * @type {boolean}
   * Flag used to determine whether or not the user is a first time user.
   */
  firstTime = true;

  constructor(
    private http: Http,
    configService: ConfigService,
    private notificationService: NotificationService,
    oauthService: OAuthService,
    private router: Router,
  ) {
    const apiEndpoint: string = configService.getSettings().apiEndpoint;
    this.url = apiEndpoint.substring(0, apiEndpoint.lastIndexOf('/')) + '/setup';
    this.args = {
      headers: new Headers({
        'Authorization': `Bearer ${oauthService.getAccessToken()}`,
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
        console.log('Response: ' + JSON.stringify(response));
        switch(response.status) {
          case 204:
          default:
            this.firstTime = true;
            break;
          case 410:
            this.firstTime = false;
            break;
        }

        // 204 (No Content) if not configured
        return response.status === 204 ? true : this.handleError('Failed to check GitHub credentials', response);
      })
      .catch((response: Response) => {
        // 410 (Gone) if already configured
        console.log('Response: ' + JSON.stringify(response));
        return response.status === 410 ? false : this.handleError('Failed to check GitHub credentials', response);
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
