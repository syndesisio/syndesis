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
   * @returns {Promise<boolean>}
   */
  isSetupPending(): Promise<boolean> {
    return this.http.get(this.url, this.args)
      .toPromise()
      .then(response => {
        // 204 (No Content) if not configured
        return response.status === 204 ? true : this.handleError('Failed to check GitHub credentials', response);
      })
      .catch((response: Response) => {
        // 410 (Gone) if already configured
        return response.status === 410 ? false : this.handleError('Failed to check GitHub credentials', response);
      });
  }

  /**
   * Function that causes redirect to setup page if GitHub account setup is required.
   * The assumption in this case is that this is likely a first time user.
   */
  public redirectToSetupIfRequired() {
    this.isSetupPending()
      .then(setupPending => {
        if (setupPending) {
          this.router.navigate(['setup']);
        } else {
          return false;
        }
      })
      .catch(message => {
        this.notificationService.message(
          NotificationType.DANGER,
          'Error',
          message,
          false,
          undefined,
          undefined,
        );
        return false;
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
