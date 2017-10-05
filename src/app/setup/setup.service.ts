import { Injectable } from '@angular/core';
import { Http, RequestOptionsArgs, Headers, Response } from '@angular/http';
import { Setup } from '../model';
import { log } from '../logging';

@Injectable()
export class SetupService {

  constructor(private http: Http) {
  }

  /**
   * Function that checks if GitHub account setup is required or has already been completed.
   * Simultaneously sets the firstTime flag.
   * 204 status from API = GitHub setup required
   * 410 status from API = GitHub setup NOT required
   * @returns {Promise<boolean>}
   */
  isSetupPending(apiEndpoint: string, accessToken: string): Promise<boolean> {
    if (apiEndpoint && accessToken) {
      const url = this.createHttpUrl(apiEndpoint);
      const args = this.createHttpArgs(accessToken);
      return this.http.get(url, args)
        .toPromise()
        .then(response => {
          if (response.status === 204) {
            return true;
          } else {
            return this.handleError('Failed to check GitHub credentials', response);
          }
        })
        .catch((response: Response) => {
          if (response.status === 410) {
            return false;
          } else {
            return this.handleError('Failed to check GitHub credentials', response);
          }
        });
    } else {
      return Promise.resolve(false);
    }
  }

  /**
   * Step 2 - Save GitHub credentials or update the setup.
   * @param {Setup} setup
   * @param {string} apiEndpoint
   * @param {string} accessToken
   * @returns {Promise<any>}
   */
  updateSetup(setup: Setup, apiEndpoint: string, accessToken: string): Promise<any> {
    const url = this.createHttpUrl(apiEndpoint);
    const args = this.createHttpArgs(accessToken);
    const body = JSON.stringify(setup);
    return this.http.put(url, body, args)
      .toPromise()
      .then(function(response) {
        // Clear session storage before trying again.
        this.oauthService.logOut(true);
        log.debug('Result: ' + JSON.stringify(response));
        // And kick off the login flow again.
        return this.oauthService.initImplicitFlow('autolink');
      })
      .catch((response: Response) => {
        // 410 (Gone) if already configured
        return response.status === 410 ? null : this.handleError('Failed to save GitHub credentials', response);
      });
  }

  /**
   * Creates HTTP URL for setup page
   * @param {string} apiEndpoint
   * @returns {string}
   */
  private createHttpUrl(apiEndpoint: string): string {
    return apiEndpoint.substring(0, apiEndpoint.lastIndexOf('/')) + '/setup';
  }

  /**
   * Creates HTTP arguments
   * @param {string} accessToken
   * @returns {RequestOptionsArgs}
   */
  private createHttpArgs(accessToken: string): RequestOptionsArgs {
    return {
      headers: new Headers({
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${accessToken}`,
      }),
    };
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
