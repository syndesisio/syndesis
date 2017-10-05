import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
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
export class GitHubOAuthSetupComponent implements OnInit {

  stepOneComplete = false;
  stepTwoComplete = false;
  //stepThreeComplete = false;
  loading: Observable<boolean>;
  //noAccountConnected = true;
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
    public detector: ChangeDetectorRef,
  ) {}

  /**
   * Step Two
   * Log user out if everything is okay. Kick off the login flow again.
   */
  connectGitHub() {
    this.updateGitHubOauthConfiguration().then(function() {
      this.stepTwoComplete = true;
      this.oauthService.logOut(true);
      return this.oauthService.initImplicitFlow('autolink');
    }).catch(function(message) {
      this.notificationService.message(NotificationType.DANGER, 'Error', message, false, null, []);
    });
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
   * Disconnects a previously connected GitHub account
   */
  disconnectGitHub() {}

  /**
   * All steps have been completed
   */
  getStarted() {
    window.open('/dashboard');
  }

  /**
   * Step One
   * Registers Syndesis as an OAuth application on GitHub
   */
  registerSyndesis() {
    this.stepOneComplete = true;
    window.open('https://github.com/settings/applications/new', '_blank');
  }

  /**
   * Returns whether or not this item has stored credentials
   */
  isConfigured(item) {
    const client = item.client || {};
    return (
      client.clientId &&
      client.clientId !== '' &&
      (client.clientSecret && client.clientSecret !== '')
    );
  }

  /**
   * View initialization
    */
  ngOnInit(): void {
  }

}
