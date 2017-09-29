import { Injectable } from '@angular/core';
import { Http, RequestOptionsArgs, Headers } from '@angular/http';
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

  isPending(): Promise<boolean> {
    return this.http.get(this.url, this.args)
      .toPromise()
      .then(response => true)
      .catch(response => false);
  }

  update(setup: Setup): Promise<any> {
    return this.http.put(this.url, JSON.stringify(setup), this.args)
      .toPromise()
      .then(response => undefined)
      .catch(response => {
        const message = 'Failed to update setup';
        log.error(message, new Error(response));
        throw message;
      });
  }

}
