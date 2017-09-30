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

  constructor(private http: Http, configService: ConfigService, oauthService: OAuthService) {
    const apiEndpoint: string = configService.getSettings().apiEndpoint;
    this.url = apiEndpoint.substring(0, apiEndpoint.lastIndexOf('/')) + '/setup';
    this.args = {
      headers: new Headers({
        'Authorization': `Bearer ${oauthService.getAccessToken()}`,
      }),
    };
  }

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

  updateSetup(setup: Setup): Promise<any> {
    return this.http.put(this.url, JSON.stringify(setup), this.args)
      .toPromise()
      .then(response => null)
      .catch((response: Response) => {
        // 410 (Gone) if already configured
        return response.status === 410 ? null : this.handleError('Failed to save GitHub credentials', response);
      });
  }

  private handleError(message: string, response: Response): Promise<any> {
    log.error(message, new Error(response.toString()));
    return Promise.reject(message);
  }

}
