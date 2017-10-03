import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { NotificationService, NotificationType } from 'patternfly-ng';
import { Observable } from 'rxjs/Observable';
import { SetupService } from './setup.service';

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
  stepThreeComplete = false;
  loading: Observable<boolean>;
  noAccountConnected = true;
  githubOauthForm = new FormGroup({
    clientId: new FormControl('', Validators.required),
    clientSecret: new FormControl('', Validators.required),
  });

  /**
   * @param {SetupService} setupService
   * @param {ChangeDetectorRef} detector
   * @param {NotificationService} notificationService
   */
  constructor(
    private setupService: SetupService,
    private notificationService: NotificationService,
    public detector: ChangeDetectorRef,
  ) {}

  /**
   * Step Two
   */
  connectGitHub() {
    this.updateGitHubOauthConfiguration();
  }

  private updateGitHubOauthConfiguration(): Promise<any> {
    const formModel = this.githubOauthForm.value;
    const setup = {
      gitHubOAuthConfiguration: {
        clientId: formModel.clientId,
        clientSecret: formModel.clientSecret,
      },
    };
    return this.setupService.updateSetup(setup)
      .catch(message => {
      this.notificationService.message(NotificationType.DANGER, 'Error', message, false, null, []);
      },
    );
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
