import { Component, ChangeDetectorRef } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { NotificationService, NotificationType } from 'patternfly-ng';
import { Observable } from 'rxjs/Observable';
import { SetupService } from './setup.service';
import { ConfigService } from '../config.service';
import { OAuthService } from 'angular-oauth2-oidc-hybrid';

export interface OAuthAppListItem {
  expanded: boolean;
}

@Component({
  selector: 'syndesis-github-oauth',
  templateUrl: 'github-oauth.component.html',
  styleUrls: ['./github-oauth.component.scss'],
})
export class GitHubOAuthSetupComponent {

  callbackUrl = this.getCallbackUrl();
  stepOneComplete = false;
  stepTwoComplete = false;
  githubOauthForm = new FormGroup({
    clientId: new FormControl('', Validators.required),
    clientSecret: new FormControl('', Validators.required),
  });

  /**
   * @param {ConfigService} configService
   * @param {OAuthService} oauthService
   * @param {SetupService} setupService
   * @param {ChangeDetectorRef} detector
   * @param {NotificationService} notificationService
   */
  constructor(
    private configService: ConfigService,
    private oauthService: OAuthService,
    private setupService: SetupService,
    private notificationService: NotificationService,
    private detector: ChangeDetectorRef,
    private router: Router,
  ) {}

  /**
   * Step Two
   * Log user out if everything is okay. Kick off the login flow again.
   */
  connectGitHub() {
    this.updateGitHubOauthConfiguration()
      .then(() => {
        this.stepTwoComplete = true;
        this.detector.detectChanges();
      }).catch(message => {
        this.notificationService.message(NotificationType.DANGER, 'Error', message, false, null, []);
      });
  }

  /**
   * Retrieves the callback URL based on the window location HREF.
   */
  getCallbackUrl() {
    const pathArray = location.href.split( '/' );
    const protocol = pathArray[0];
    const host = pathArray[2];
    return protocol + '//' + host;
  }

  /**
   * Updates GitHub OAuth configuration
   * @returns {Promise<any>}
   */
  private updateGitHubOauthConfiguration(): Promise<any> {
    const formModel = this.githubOauthForm.value;
    const setup = {
      gitHubOAuthConfiguration: {
        clientId: formModel.clientId,
        clientSecret: formModel.clientSecret,
      },
    };
    const apiEndpoint = this.configService.getSettings().apiEndpoint;
    const accessToken = this.oauthService.getAccessToken();
    return this.setupService.updateSetup(setup, apiEndpoint, accessToken);
  }

  /**
   * All steps have been completed
   */
  getStarted() {
    window.location.assign('/');
  }

  /**
   * Step One
   * Registers Syndesis as an OAuth application on GitHub
   */
  registerSyndesis() {
    this.stepOneComplete = true;
    window.open('https://github.com/settings/applications/new', '_blank');
  }

}
