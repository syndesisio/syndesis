import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Notification, NotificationModule, NotificationService, NotificationType } from 'patternfly-ng';
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
   * @param {ChangeDetectorRef} detector
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
    return this.setupService.update(setup)
      .catch(message => {
        const notification: Notification = {
          type          : NotificationType.WARNING,
          header        : 'Error',
          message       : message,
          showClose     : true,
        };
        this.notificationService.getNotifications().push(notification);
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
  getStarted() {}

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
