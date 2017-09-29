import { Injectable } from '@angular/core';
import { Http, RequestOptionsArgs, Headers } from '@angular/http';
import { OAuthService } from 'angular-oauth2-oidc-hybrid';
import { ConfigService } from '../config.service';
import { GitHubOAuthConfiguration } from '../model';

@Injectable()
export class GitHubOAuthService {

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

  update(configuration: GitHubOAuthConfiguration) {
    return this.http.put(this.url, JSON.stringify(configuration), this.args);
  }

}
